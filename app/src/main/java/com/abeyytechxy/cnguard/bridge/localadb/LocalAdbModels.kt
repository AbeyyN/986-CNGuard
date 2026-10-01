package com.abeyytechxy.cnguard.bridge.localadb

enum class AdbServiceKind {
    PAIRING,
    CONNECT
}

data class LocalAdbService(
    val kind: AdbServiceKind,
    val serviceName: String
)

enum class LocalBridgeState {
    IDLE,
    DISCOVERING,
    AVAILABLE,
    PAIRING_REQUIRED,
    UNAVAILABLE,
    ERROR
}

data class LocalBridgeSnapshot(
    val state: LocalBridgeState,
    val pairingService: LocalAdbService? = null,
    val connectService: LocalAdbService? = null,
    val detail: String? = null
)
