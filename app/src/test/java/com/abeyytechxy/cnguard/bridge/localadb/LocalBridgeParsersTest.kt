package com.abeyytechxy.cnguard.bridge.localadb

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalBridgeParsersTest {
    @Test
    fun pairingCodeRequiresExactlySixDigits() {
        assertTrue(PairingCodeValidator.isValid("123456"))
        assertTrue(PairingCodeValidator.isValid(" 123456 "))
        assertFalse(PairingCodeValidator.isValid("12345"))
        assertFalse(PairingCodeValidator.isValid("1234567"))
        assertFalse(PairingCodeValidator.isValid("12A456"))
    }

    @Test
    fun packageUidParserFindsRequestedPackage() {
        val output = """
            package:com.example uid:10123
            package:com.google.android.gms uid:10012
        """.trimIndent()

        assertEquals(
            10012,
            PackageUidParser.parse(output, "com.google.android.gms")
        )
    }

    @Test
    fun fcmSocketParserRequiresUidPortAndEstablishedState() {
        val established = """
             sl  local_address rem_address   st tx_queue:rx_queue tr:tm->when retrnsmt   uid  timeout inode
              0: 0100007F:C001 08080808:146C 01 00000000:00000000 00:00000000 00000000 10012 0 0
        """.trimIndent()

        assertTrue(FcmSocketParser.hasEstablishedSocket(established, 10012))
        assertFalse(FcmSocketParser.hasEstablishedSocket(established, 10013))

        val wrongState = established.replace(" 01 00000000", " 0A 00000000")
        assertFalse(FcmSocketParser.hasEstablishedSocket(wrongState, 10012))
    }
}
