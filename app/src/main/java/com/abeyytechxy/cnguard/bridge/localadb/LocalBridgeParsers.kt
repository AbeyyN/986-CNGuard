package com.abeyytechxy.cnguard.bridge.localadb

object PairingCodeValidator {
    private val pattern = Regex("^\\d{6}$")

    fun isValid(code: String): Boolean = pattern.matches(code.trim())
}

object PackageUidParser {
    fun parse(output: String, packageName: String): Int? {
        val packagePrefix = "package:$packageName "
        return output.lineSequence()
            .map(String::trim)
            .firstOrNull { it.startsWith(packagePrefix) }
            ?.substringAfter("uid:")
            ?.substringBefore(' ')
            ?.trim()
            ?.toIntOrNull()
    }
}

object FcmSocketParser {
    private const val TCP_ESTABLISHED = "01"
    private val whitespace = Regex("\\s+")
    private val fcmPorts = 5228..5230

    fun hasEstablishedSocket(raw: String, gmsUid: Int): Boolean {
        return raw.lineSequence().any { line ->
            val fields = line.trim().split(whitespace)
            if (fields.size <= 7 || fields[3] != TCP_ESTABLISHED) {
                return@any false
            }

            val remotePort = fields[2]
                .substringAfterLast(':', missingDelimiterValue = "")
                .toIntOrNull(16)

            val uid = fields[7].toIntOrNull()
            uid == gmsUid && remotePort in fcmPorts
        }
    }
}
