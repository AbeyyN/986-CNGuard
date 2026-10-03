package com.abeyytechxy.cnguard.fixes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GuidedFixPlannerTest {
    @Test fun appPageRequiresValidPackage() {
        assertNull(GuidedFixPlanner.create(GuidedFixAction.APP_NOTIFICATIONS, null))
        assertNull(GuidedFixPlanner.create(GuidedFixAction.APP_NOTIFICATIONS, "bad; command"))
        assertNull(GuidedFixPlanner.create(GuidedFixAction.APP_DETAILS, "not-a-package"))
        assertFalse(GuidedFixPlanner.isValidPackage("com.example/../files"))
        assertTrue(GuidedFixPlanner.isValidPackage("com.google.android.gms"))
    }

    @Test fun appNavigationKeepsValidatedTarget() {
        val plan = GuidedFixPlanner.create(
            GuidedFixAction.APP_NOTIFICATIONS, " com.whatsapp "
        )
        assertNotNull(plan)
        assertEquals("com.whatsapp", plan?.packageName)
    }

    @Test fun systemPagesDoNotRequireOrRetainPackage() {
        val autostart = GuidedFixPlanner.create(
            GuidedFixAction.XIAOMI_AUTOSTART, "org.telegram.messenger"
        )
        val battery = GuidedFixPlanner.create(GuidedFixAction.BATTERY_OPTIMIZATION)
        assertNotNull(autostart)
        assertNotNull(battery)
        assertNull(autostart?.packageName)
        assertNull(battery?.packageName)
    }
}
