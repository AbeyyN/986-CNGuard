package com.abeyytechxy.cnguard.diagnostics

import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceFamilyClassifierTest {
    @Test fun xiaomiManufacturerIsFamilySignalNotCnProof() {
        assertEquals(
            XiaomiFamilyState.XIAOMI_FAMILY,
            DeviceFamilyClassifier.classify("Xiaomi", "Xiaomi", true, true)
        )
    }

    @Test fun redmiBrandNeedsVendorServiceCorroboration() {
        assertEquals(
            XiaomiFamilyState.UNCONFIRMED,
            DeviceFamilyClassifier.classify("unknown", "Redmi", null, null)
        )
        assertEquals(
            XiaomiFamilyState.XIAOMI_FAMILY,
            DeviceFamilyClassifier.classify("unknown", "Redmi", true, false)
        )
    }

    @Test fun pocoAndNonXiaomiAreDifferent() {
        assertEquals(
            XiaomiFamilyState.XIAOMI_FAMILY,
            DeviceFamilyClassifier.classify("unknown", "POCO", false, true)
        )
        assertEquals(
            XiaomiFamilyState.OTHER_MANUFACTURER,
            DeviceFamilyClassifier.classify("Samsung", "samsung", false, false)
        )
    }
}
