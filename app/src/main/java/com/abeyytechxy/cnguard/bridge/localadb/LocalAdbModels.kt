package com.abeyytechxy.cnguard.bridge.localadb

enum class AdbServiceKind {
    PAIRING,
    CONNECT
}

data class LocalAdbService(
    val kind: AdbServiceKind,
    val serviceName: String,
    val host: String? = null,
    val port: Int? = null
) {
    val isResolved: Boolean
        get() = !host.isNullOrBlank() && port != null && port in 1..65535
}

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

data class LocalBridgeResult(
    val success: Boolean,
    val summary: String,
    val detail: String? = null
)

data class LocalBridgeDiagnostic(
    val id: String,
    val available: Boolean,
    val summary: String,
    val detail: String? = null
)
