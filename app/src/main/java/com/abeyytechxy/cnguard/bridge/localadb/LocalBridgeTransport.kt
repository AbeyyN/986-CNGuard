package com.abeyytechxy.cnguard.bridge.localadb

import android.content.Context
import com.flyfishxu.kadb.Kadb
import com.flyfishxu.kadb.cert.KadbCert
import com.flyfishxu.kadb.cert.OkioFilePrivateKeyStore
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toOkioPath
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LocalBridgeTransport(context: Context) {
    companion object {
        private const val GMS_PACKAGE = "com.google.android.gms"
        private const val CONNECT_TIMEOUT_MS = 5_000
        private const val SOCKET_TIMEOUT_MS = 10_000

        private val identityLock = Any()
        @Volatile
        private var identityConfigured = false
    }

    private val appContext = context.applicationContext
    private val executor: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "cnguard-local-bridge").apply { isDaemon = true }
    }

    init {
        configureIdentity()
    }

    fun pair(
        service: LocalAdbService?,
        pairingCode: String,
        callback: (LocalBridgeResult) -> Unit
    ) {
        val endpoint = resolvedEndpoint(service)
            ?: return callback(LocalBridgeResult(false, "Pairing endpoint unavailable"))

        val normalizedCode = pairingCode.trim()
        if (!PairingCodeValidator.isValid(normalizedCode)) {
            callback(LocalBridgeResult(false, "Pairing code must contain exactly 6 digits"))
            return
        }

        executor.execute {
            val result = runCatching {
                runBlocking {
                    Kadb.pair(
                        host = endpoint.first,
                        port = endpoint.second,
                        pairingCode = normalizedCode,
                        name = "986 CN Guard"
                    )
                }
                LocalBridgeResult(true, "Pairing completed")
            }.getOrElse { error ->
                LocalBridgeResult(
                    success = false,
                    summary = "Pairing failed",
                    detail = safeError(error)
                )
            }
            callback(result)
        }
    }

    fun testConnection(
        service: LocalAdbService?,
        callback: (LocalBridgeResult) -> Unit
    ) {
        val endpoint = resolvedEndpoint(service)
            ?: return callback(LocalBridgeResult(false, "Connect endpoint unavailable"))

        executor.execute {
            val result = runCatching {
                open(endpoint).use { adb ->
                    val response = adb.shell("echo 986-cnguard")
                    val ok = response.exitCode == 0 && response.output.trim() == "986-cnguard"
                    if (!ok) {
                        LocalBridgeResult(
                            false,
                            "ADB connection test failed",
                            response.errorOutput.ifBlank { response.output }.trim().take(500)
                        )
                    } else {
                        LocalBridgeResult(true, "ADB connection verified")
                    }
                }
            }.getOrElse { error ->
                LocalBridgeResult(
                    false,
                    "ADB connection failed",
                    safeError(error)
                )
            }
            callback(result)
        }
    }

    fun runReadOnlyDiagnostics(
        service: LocalAdbService?,
        callback: (List<LocalBridgeDiagnostic>) -> Unit
    ) {
        val endpoint = resolvedEndpoint(service)
        if (endpoint == null) {
            callback(
                listOf(
                    LocalBridgeDiagnostic(
                        id = "bridge.endpoint",
                        available = false,
                        summary = "Connect endpoint unavailable"
                    )
                )
            )
            return
        }

        executor.execute {
            val diagnostics = runCatching {
                open(endpoint).use { adb ->
                    buildList {
                        add(probeGreezer(adb))
                        add(probeFcmSocket(adb))
                    }
                }
            }.getOrElse { error ->
                listOf(
                    LocalBridgeDiagnostic(
                        id = "bridge.connection",
                        available = false,
                        summary = "Could not run Local Bridge diagnostics",
                        detail = safeError(error)
                    )
                )
            }
            callback(diagnostics)
        }
    }

    fun close() {
        executor.shutdownNow()
    }

    private fun probeGreezer(adb: Kadb): LocalBridgeDiagnostic {
        val response = runCatching { adb.shell("dumpsys greezer") }.getOrElse { error ->
            return LocalBridgeDiagnostic(
                id = "xiaomi.greezer",
                available = false,
                summary = "Greezer service unavailable",
                detail = safeError(error)
            )
        }

        val output = response.output.trim()
        val available = response.exitCode == 0 &&
            output.isNotBlank() &&
            !output.contains("Can't find service", ignoreCase = true)

        return LocalBridgeDiagnostic(
            id = "xiaomi.greezer",
            available = available,
            summary = if (available) {
                "Greezer service is readable through Local Bridge"
            } else {
                "Greezer service not detected"
            },
            detail = output.lineSequence().firstOrNull()?.take(300)
        )
    }

    private fun probeFcmSocket(adb: Kadb): LocalBridgeDiagnostic {
        val uidResponse = runCatching {
            adb.shell("pm list packages --user 0 -U $GMS_PACKAGE")
        }.getOrElse { error ->
            return LocalBridgeDiagnostic(
                id = "fcm.socket",
                available = false,
                summary = "Google Play services UID could not be read",
                detail = safeError(error)
            )
        }

        val gmsUid = PackageUidParser.parse(uidResponse.output, GMS_PACKAGE)
            ?: return LocalBridgeDiagnostic(
                id = "fcm.socket",
                available = false,
                summary = "Google Play services UID not found"
            )

        val socketResponse = runCatching {
            adb.shell("cat /proc/net/tcp /proc/net/tcp6")
        }.getOrElse { error ->
            return LocalBridgeDiagnostic(
                id = "fcm.socket",
                available = false,
                summary = "FCM socket table is not readable",
                detail = safeError(error)
            )
        }

        if (socketResponse.exitCode != 0) {
            return LocalBridgeDiagnostic(
                id = "fcm.socket",
                available = false,
                summary = "FCM socket table is not readable",
                detail = socketResponse.errorOutput.trim().take(300)
            )
        }

        val matched = FcmSocketParser.hasEstablishedSocket(socketResponse.output, gmsUid)
        return LocalBridgeDiagnostic(
            id = "fcm.socket",
            available = true,
            summary = if (matched) {
                "Established Google Play services FCM socket detected"
            } else {
                "No established Google Play services FCM socket detected"
            },
            detail = "GMS uid=$gmsUid · ports 5228-5230"
        )
    }

    private fun open(endpoint: Pair<String, Int>): Kadb {
        return Kadb.create(
            host = endpoint.first,
            port = endpoint.second,
            connectTimeout = CONNECT_TIMEOUT_MS,
            socketTimeout = SOCKET_TIMEOUT_MS
        )
    }

    private fun resolvedEndpoint(service: LocalAdbService?): Pair<String, Int>? {
        if (service?.isResolved != true) return null
        val host = service.host ?: return null
        val port = service.port ?: return null
        return host to port
    }

    private fun configureIdentity() {
        if (identityConfigured) return

        synchronized(identityLock) {
            if (identityConfigured) return

            val identityDirectory = File(appContext.filesDir, "local-bridge")
            check(identityDirectory.exists() || identityDirectory.mkdirs()) {
                "Could not create Local Bridge identity directory"
            }

            val privateKeyFile = File(identityDirectory, "adb-private-key.pem")
            KadbCert.configure(
                store = OkioFilePrivateKeyStore(privateKeyFile.toOkioPath())
            )
            KadbCert.ensureReady()
            identityConfigured = true
        }
    }

    private fun safeError(error: Throwable): String {
        return (error.message ?: error.javaClass.simpleName)
            .replace(appContext.filesDir.absolutePath, "<app-files>")
            .take(500)
    }
}
