package com.abeyytechxy.cnguard.diagnostics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FcmNetworkPolicyTest {
    @Test fun noDnsIsUnknownNotProofOfBlockedFcm() {
        val result = FcmNetworkPolicy.evaluate(false, emptyList())
        assertEquals(FcmNetworkState.DNS_UNAVAILABLE, result.state)
    }

    @Test fun reachablePortDoesNotClaimFcmDelivery() {
        val result = FcmNetworkPolicy.evaluate(true, listOf(5228, 443))
        assertEquals(FcmNetworkState.TCP_ENDPOINT_REACHABLE, result.state)
        assertEquals(listOf(5228, 443), result.reachablePorts)
        assertTrue(result.summary.contains("does not prove FCM"))
    }

    @Test fun noTcpConnectionCannotDeclareCompleteBlock() {
        val result = FcmNetworkPolicy.evaluate(true, emptyList())
        assertEquals(FcmNetworkState.NO_TCP_CONNECTION_OBSERVED, result.state)
        assertTrue(result.summary.contains("does not prove FCM is blocked"))
    }

    @Test fun unknownPortsCannotAppearAsReachable() {
        val result = FcmNetworkPolicy.evaluate(true, listOf(22, 9000))
        assertTrue(result.reachablePorts.isEmpty())
    }
}
