package com.abeyytechxy.cnguard.bridge.localadb

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
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

        startDiscovery(
            serviceType = PAIRING_SERVICE_TYPE,
            kind = AdbServiceKind.PAIRING
        )
        startDiscovery(
            serviceType = CONNECT_SERVICE_TYPE,
            kind = AdbServiceKind.CONNECT
        )

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
                val service = LocalAdbService(
                    kind = kind,
                    serviceName = serviceInfo.serviceName
                )

                when (kind) {
                    AdbServiceKind.PAIRING -> pairingService = service
                    AdbServiceKind.CONNECT -> connectService = service
                }
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

    private fun finishDiscovery() {
        if (!active.compareAndSet(true, false)) {
            return
        }

        handler.removeCallbacks(finishRunnable)

        listeners.toList().forEach(::stopListener)
        listeners.clear()
        releaseMulticastLock()

        val snapshot = when {
            connectService != null -> LocalBridgeSnapshot(
                state = LocalBridgeState.AVAILABLE,
                pairingService = pairingService,
                connectService = connectService,
                detail = "Wireless ADB connect service detected"
            )

            pairingService != null -> LocalBridgeSnapshot(
                state = LocalBridgeState.PAIRING_REQUIRED,
                pairingService = pairingService,
                detail = "ADB pairing service detected"
            )

            lastError != null -> LocalBridgeSnapshot(
                state = LocalBridgeState.ERROR,
                detail = lastError
            )

            else -> LocalBridgeSnapshot(
                state = LocalBridgeState.UNAVAILABLE,
                detail = "No Wireless ADB service detected"
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

        multicastLock = wifiManager
            .createMulticastLock("986CNGuard:LocalAdbDiscovery")
            .apply {
                setReferenceCounted(false)
                acquire()
            }
    }

    private fun releaseMulticastLock() {
        multicastLock?.let { lock ->
            if (lock.isHeld) {
                lock.release()
            }
        }
        multicastLock = null
    }
}
