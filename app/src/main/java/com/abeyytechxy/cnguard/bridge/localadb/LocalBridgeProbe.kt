package com.abeyytechxy.cnguard.bridge.localadb

enum class LocalBridgeProbeId {
    READ_GREEZER_GMS_STATE,
    READ_FCM_SOCKET_STATE,
    READ_XIAOMI_AUTOSTART_STATE
}

data class LocalBridgeProbeSpec(
    val id: LocalBridgeProbeId,
    val requiresShell: Boolean
)

object LocalBridgeProbeCatalog {
    val probes: Map<LocalBridgeProbeId, LocalBridgeProbeSpec> =
        listOf(
            LocalBridgeProbeSpec(
                id = LocalBridgeProbeId.READ_GREEZER_GMS_STATE,
                requiresShell = true
            ),
            LocalBridgeProbeSpec(
                id = LocalBridgeProbeId.READ_FCM_SOCKET_STATE,
                requiresShell = true
            ),
            LocalBridgeProbeSpec(
                id = LocalBridgeProbeId.READ_XIAOMI_AUTOSTART_STATE,
                requiresShell = true
            )
        ).associateBy(LocalBridgeProbeSpec::id)
}
