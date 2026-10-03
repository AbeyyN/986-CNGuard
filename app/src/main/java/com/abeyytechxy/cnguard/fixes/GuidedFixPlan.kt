package com.abeyytechxy.cnguard.fixes

enum class GuidedFixAction {
    APP_NOTIFICATIONS,
    APP_DETAILS,
    XIAOMI_AUTOSTART,
    BATTERY_OPTIMIZATION
}

data class GuidedFixPlan(
    val action: GuidedFixAction,
    val packageName: String?,
    val instructions: String
)

object GuidedFixPlanner {
    private val packagePattern =
        Regex("^[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)+$")

    fun create(action: GuidedFixAction, packageName: String? = null): GuidedFixPlan? {
        val appAction = action == GuidedFixAction.APP_NOTIFICATIONS ||
            action == GuidedFixAction.APP_DETAILS

        val normalizedPackage = packageName?.trim()
        if (appAction && !isValidPackage(normalizedPackage)) return null

        return when (action) {
            GuidedFixAction.APP_NOTIFICATIONS -> GuidedFixPlan(
                action,
                normalizedPackage,
                "Review notification permission, alert importance and lock-screen behavior. Return to CN Guard and recheck."
            )

            GuidedFixAction.APP_DETAILS -> GuidedFixPlan(
                action,
                normalizedPackage,
                "Review the selected app's background and battery restrictions. Return to CN Guard and recheck."
            )

            GuidedFixAction.XIAOMI_AUTOSTART -> GuidedFixPlan(
                action,
                null,
                "Review Autostart in Xiaomi Security Center if available. Changes are performed by you, not CN Guard."
            )

            GuidedFixAction.BATTERY_OPTIMIZATION -> GuidedFixPlan(
                action,
                null,
                "Review Android battery optimization settings. Do not disable restrictions indiscriminately."
            )
        }
    }

    fun isValidPackage(packageName: String?): Boolean =
        packageName != null &&
            packageName.length in 3..255 &&
            packagePattern.matches(packageName)
}
