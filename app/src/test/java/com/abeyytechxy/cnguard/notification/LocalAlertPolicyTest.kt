package com.abeyytechxy.cnguard.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalAlertPolicyTest {
    @Test fun disabledAppCannotRunTest() {
        val result = LocalAlertPolicy.assess(false, 4)
        assertEquals(LocalAlertState.APP_NOTIFICATIONS_DISABLED, result.state)
    }

    @Test fun blockedChannelCannotRunTest() {
        val result = LocalAlertPolicy.assess(true, 0)
        assertEquals(LocalAlertState.CHANNEL_BLOCKED, result.state)
    }

    @Test fun lowChannelDoesNotPromisePopup() {
        val result = LocalAlertPolicy.assess(true, 2)
        assertEquals(LocalAlertState.CHANNEL_NOT_HIGH_IMPORTANCE, result.state)
        assertTrue(result.summary.contains("not expected"))
    }

    @Test fun highChannelIsOnlyEligibleNotVerified() {
        val result = LocalAlertPolicy.assess(true, 4)
        assertEquals(LocalAlertState.ELIGIBLE_FOR_HIGH_PRIORITY_ALERT, result.state)
        assertTrue(result.summary.contains("unverified"))
    }

    @Test fun missingChannelStateRemainsUnknown() {
        val result = LocalAlertPolicy.assess(true, null)
        assertEquals(LocalAlertState.UNAVAILABLE, result.state)
    }
}
