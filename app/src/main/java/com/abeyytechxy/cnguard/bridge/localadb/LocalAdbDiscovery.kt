package com.abeyytechxy.cnguard.bridge.localadb

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import java.net.NetworkInterface
import java.util.concurrent.atomic.AtomicBoolean

class LocalAdbDiscovery(context: Context) {
    companion object {
        private const val PAIRING_SERVICE_TYPE = "_adb-tls-pairing._tcp"
        private const val CONNECT_SERVICE_TYPE = "_adb-tls-connect._tcp"
        private const val DEFAULT_TIMEOUT_MS = 5_000L
    }

    private val appContext = context.applicationContext
    private val nsdManager = appContext.getSystemService(NsdManager::class.java)
    private val wifiManager = appContext.getSystemService(WifiManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private val finishRunnable = Runnable { finishDiscovery() }

    private val active = AtomicBoolean(false)
    private val listeners = mutableListOf<NsdManager.DiscoveryListener>()

    private var multicastLock: WifiManager.MulticastLock? = null
    private var pairingService: LocalAdbService? = null
    private var connectService: LocalAdbService? = null
    private var lastError: String? = null
    private var completion: ((LocalBridgeSnapshot) -> Unit)? = null

    fun discover(
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
        callback: (LocalBridgeSnapshot) -> Unit
    ) {
        if (!active.compareAndSet(false, true)) {
            callback(
                LocalBridgeSnapshot(
                    state = LocalBridgeState.DISCOVERING,
                    detail = "Discovery is already running"
                )
            )
            return
        }

        pairingService = null
        connectService = null
        lastError = null
        completion = callback

        acquireMulticastLock()

        startDiscovery(PAIRING_SERVICE_TYPE, AdbServiceKind.PAIRING)
        startDiscovery(CONNECT_SERVICE_TYPE, AdbServiceKind.CONNECT)

        handler.postDelayed(finishRunnable, timeoutMs.coerceAtLeast(1_000L))
    }

    fun close() {
        if (active.get()) {
            finishDiscovery()
        } else {
            releaseMulticastLock()
        }
    }

    private fun startDiscovery(
        serviceType: String,
        kind: AdbServiceKind
    ) {
        val listener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) = Unit

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                resolveService(serviceInfo, kind)
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                when (kind) {
                    AdbServiceKind.PAIRING -> {
                        if (pairingService?.serviceName == serviceInfo.serviceName) {
                            pairingService = null
                        }
                    }

                    AdbServiceKind.CONNECT -> {
                        if (connectService?.serviceName == serviceInfo.serviceName) {
                            connectService = null
                        }
                    }
                }
            }

            override fun onDiscoveryStopped(serviceType: String) = Unit

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                lastError = "mDNS discovery failed ($errorCode)"
                stopListener(this)
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                lastError = "mDNS discovery stop failed ($errorCode)"
            }
        }

        listeners += listener

        runCatching {
            nsdManager.discoverServices(
                serviceType,
                NsdManager.PROTOCOL_DNS_SD,
                listener
            )
        }.onFailure {
            lastError = it.message ?: it.javaClass.simpleName
            listeners.remove(listener)
        }
    }

    @Suppress("DEPRECATION")
    private fun resolveService(
        serviceInfo: NsdServiceInfo,
        kind: AdbServiceKind
    ) {
        runCatching {
            nsdManager.resolveService(
                serviceInfo,
                object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                        lastError = "mDNS resolve failed ($errorCode)"
                    }

                    override fun onServiceResolved(resolved: NsdServiceInfo) {
                        val host = resolvedHost(resolved) ?: return
                        val port = resolved.port
                        if (port !in 1..65535 || !isLocalDeviceAddress(host)) {
                            return
                        }

                        val service = LocalAdbService(
                            kind = kind,
                            serviceName = resolved.serviceName,
                            host = host,
                            port = port
                        )

                        when (kind) {
                            AdbServiceKind.PAIRING -> pairingService = service
                            AdbServiceKind.CONNECT -> connectService = service
                        }
                    }
                }
            )
        }.onFailure {
            lastError = it.message ?: it.javaClass.simpleName
        }
    }

    @Suppress("DEPRECATION")
    private fun resolvedHost(serviceInfo: NsdServiceInfo): String? {
        val address = if (Build.VERSION.SDK_INT >= 34) {
            serviceInfo.hostAddresses.firstOrNull()
        } else {
            serviceInfo.host
        }
        return address?.hostAddress?.substringBefore('%')
    }

    private fun isLocalDeviceAddress(host: String): Boolean {
        val normalized = host.substringBefore('%')
        return runCatching {
            NetworkInterface.getNetworkInterfaces()
                ?.toList()
                .orEmpty()
                .flatMap { networkInterface ->
                    networkInterface.inetAddresses.toList()
                }
                .any { address ->
                    address.hostAddress?.substringBefore('%') == normalized
                }
        }.getOrDefault(false)
    }

    private fun finishDiscovery() {
        if (!active.compareAndSet(true, false)) {
            return
        }

        handler.removeCallbacks(finishRunnable)

        listeners.toList().forEach(::stopListener)
        listeners.clear()
        releaseMulticastLock()

        val snapshot = when {
            connectService?.isResolved == true -> LocalBridgeSnapshot(
                state = LocalBridgeState.AVAILABLE,
                pairingService = pairingService,
                connectService = connectService,
                detail = "Local Wireless ADB connect service detected"
            )

            pairingService?.isResolved == true -> LocalBridgeSnapshot(
                state = LocalBridgeState.PAIRING_REQUIRED,
                pairingService = pairingService,
                detail = "Local ADB pairing service detected"
            )

            lastError != null -> LocalBridgeSnapshot(
                state = LocalBridgeState.ERROR,
                detail = lastError
            )

            else -> LocalBridgeSnapshot(
                state = LocalBridgeState.UNAVAILABLE,
                detail = "No local Wireless ADB service detected"
            )
        }

        completion?.invoke(snapshot)
        completion = null
    }

    private fun stopListener(listener: NsdManager.DiscoveryListener) {
        runCatching {
            nsdManager.stopServiceDiscovery(listener)
        }
    }

    private fun acquireMulticastLock() {
        if (multicastLock?.isHeld == true) {
            return
        }

        runCatching {
            wifiManager.createMulticastLock("986CNGuard:LocalAdbDiscovery").apply {
                setReferenceCounted(false)
                acquire()
            }
        }.onSuccess {
            multicastLock = it
        }.onFailure {
            lastError = "Wi-Fi multicast discovery unavailable"
        }
    }

    private fun releaseMulticastLock() {
        multicastLock?.let { lock ->
            runCatching {
                if (lock.isHeld) lock.release()
            }
        }
        multicastLock = null
    }
}
