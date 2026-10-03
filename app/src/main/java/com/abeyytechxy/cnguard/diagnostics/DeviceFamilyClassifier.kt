package com.abeyytechxy.cnguard.diagnostics

enum class XiaomiFamilyState {
    XIAOMI_FAMILY,
    UNCONFIRMED,
    OTHER_MANUFACTURER
}

object DeviceFamilyClassifier {
    private val familyBrands = setOf("xiaomi", "redmi", "poco")

    fun classify(
        manufacturer: String?,
        brand: String?,
        securityCenterPresent: Boolean?,
        powerKeeperPresent: Boolean?
    ): XiaomiFamilyState {
        val maker = manufacturer?.trim()?.lowercase().orEmpty()
        val branded = brand?.trim()?.lowercase() in familyBrands
        val hasXiaomiServices =
            securityCenterPresent == true || powerKeeperPresent == true

        return when {
            maker == "xiaomi" -> XiaomiFamilyState.XIAOMI_FAMILY
            branded && hasXiaomiServices -> XiaomiFamilyState.XIAOMI_FAMILY
            branded || maker.isBlank() -> XiaomiFamilyState.UNCONFIRMED
            else -> XiaomiFamilyState.OTHER_MANUFACTURER
        }
    }
}
