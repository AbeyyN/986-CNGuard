package com.abeyytechxy.cnguard.simulation

import com.abeyytechxy.cnguard.bridge.localadb.AdbServiceKind
import com.abeyytechxy.cnguard.bridge.localadb.LocalAdbService
import com.abeyytechxy.cnguard.diagnostics.DiagnosticState
import com.abeyytechxy.cnguard.diagnostics.FcmNetworkState
import com.abeyytechxy.cnguard.diagnostics.XiaomiFamilyState
import com.abeyytechxy.cnguard.notification.PresentationState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CnRomSimulationHarnessTest {
    @Test
    fun healthyCnLikeFixtureExercisesExpectedHappyPathWithoutClaimingRomProof() {
        val result = CnRomSimulationHarness.evaluate(
            CnRomSimulationFixture(
                name = "xiaomi-hyperos-cn-like-healthy",
                manufacturer = "Xiaomi",
                brand = "Xiaomi",
                securityCenterPresent = true,
                powerKeeperPresent = true,
                dnsResolved = true,
                reachablePorts = setOf(5228, 443),
                gmsUid = 10012,
                procNetTcp = establishedSocket(uid = 10012, remotePortHex = "146C"),
                notificationImportance = 4,
                packageSuspended = false,
                suppressedVisualEffects = 0,
                pairingService = resolvedService(AdbServiceKind.PAIRING, 37123),
                connectService = resolvedService(AdbServiceKind.CONNECT, 39555),
                pairingCode = "123456"
            )
        )

        assertEquals(XiaomiFamilyState.XIAOMI_FAMILY, result.family)
        assertEquals(DiagnosticState.UNKNOWN, result.romRegion)
        assertEquals(FcmNetworkState.TCP_ENDPOINT_REACHABLE, result.network)
        assertEquals(SimulatedSocketState.PRESENT, result.fcmSocket)
        assertEquals(PresentationState.INFO, result.notificationPresentation)
        assertTrue(result.pairingEndpointResolved)
        assertTrue(result.connectEndpointResolved)
        assertTrue(result.pairingCodeAccepted)
    }

    @Test
    fun restrictedCnLikeFixtureKeepsTransportAndPresentationFailuresSeparate() {
        val result = CnRomSimulationHarness.evaluate(
            CnRomSimulationFixture(
                name = "redmi-cn-like-restricted",
                manufacturer = "unknown",
                brand = "Redmi",
                securityCenterPresent = true,
                powerKeeperPresent = false,
                dnsResolved = true,
                reachablePorts = emptySet(),
                gmsUid = 10012,
                procNetTcp = establishedSocket(uid = 10099, remotePortHex = "146C"),
                notificationImportance = 2,
                packageSuspended = true,
                suppressedVisualEffects = 1,
                pairingService = LocalAdbService(
                    kind = AdbServiceKind.PAIRING,
                    serviceName = "pairing-unresolved",
                    host = "127.0.0.1",
                    port = null
                ),
                connectService = null,
                pairingCode = "12A456"
            )
        )

        assertEquals(XiaomiFamilyState.XIAOMI_FAMILY, result.family)
        assertEquals(DiagnosticState.UNKNOWN, result.romRegion)
        assertEquals(FcmNetworkState.NO_TCP_CONNECTION_OBSERVED, result.network)
        assertEquals(SimulatedSocketState.ABSENT, result.fcmSocket)
        assertEquals(PresentationState.ATTENTION, result.notificationPresentation)
        assertFalse(result.pairingEndpointResolved)
        assertFalse(result.connectEndpointResolved)
        assertFalse(result.pairingCodeAccepted)
    }

    @Test
    fun inaccessibleSocketEvidenceFailsClosedToUnknown() {
        val result = CnRomSimulationHarness.evaluate(
            CnRomSimulationFixture(
                name = "xiaomi-cn-like-probe-denied",
                manufacturer = "Xiaomi",
                brand = "Xiaomi",
                securityCenterPresent = null,
                powerKeeperPresent = null,
                dnsResolved = false,
                reachablePorts = emptySet(),
                gmsUid = 10012,
                procNetTcp = null,
                notificationImportance = 3,
                packageSuspended = false,
                suppressedVisualEffects = 0,
                pairingService = null,
                connectService = null,
                pairingCode = "000000"
            )
        )

        assertEquals(XiaomiFamilyState.XIAOMI_FAMILY, result.family)
        assertEquals(DiagnosticState.UNKNOWN, result.romRegion)
        assertEquals(FcmNetworkState.DNS_UNAVAILABLE, result.network)
        assertEquals(SimulatedSocketState.UNKNOWN, result.fcmSocket)
        assertEquals(PresentationState.INFO, result.notificationPresentation)
        assertFalse(result.pairingEndpointResolved)
        assertFalse(result.connectEndpointResolved)
        assertTrue(result.pairingCodeAccepted)
    }

    private fun resolvedService(kind: AdbServiceKind, port: Int) = LocalAdbService(
        kind = kind,
        serviceName = "fixture-$kind",
        host = "127.0.0.1",
        port = port
    )

    private fun establishedSocket(uid: Int, remotePortHex: String): String = """
        sl  local_address rem_address   st tx_queue:rx_queue tr:tm->when retrnsmt   uid  timeout inode
         0: 0100007F:C001 08080808:$remotePortHex 01 00000000:00000000 00:00000000 00000000 $uid 0 0
    """.trimIndent()
}
