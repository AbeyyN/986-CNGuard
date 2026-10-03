package com.abeyytechxy.cnguard.fixes

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.provider.Settings

sealed interface GuidedNavigationResult {
    data class Opened(val instruction: String) : GuidedNavigationResult
    data object Unavailable : GuidedNavigationResult
}

class SafeSettingsNavigator(private val activity: Activity) {
    fun open(plan: GuidedFixPlan): GuidedNavigationResult {
        val requiresPackage = plan.action == GuidedFixAction.APP_NOTIFICATIONS ||
            plan.action == GuidedFixAction.APP_DETAILS
        if (requiresPackage && !GuidedFixPlanner.isValidPackage(plan.packageName)) {
            return GuidedNavigationResult.Unavailable
        }
        val candidates = when (plan.action) {
            GuidedFixAction.APP_NOTIFICATIONS -> listOf(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, plan.packageName)
                },
                appDetails(plan.packageName)
            )

            GuidedFixAction.APP_DETAILS -> listOf(appDetails(plan.packageName))

            GuidedFixAction.XIAOMI_AUTOSTART -> listOf(
                Intent("miui.intent.action.OP_AUTO_START").apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                },
                Intent().apply {
                    component = ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                },
                Intent(Settings.ACTION_APPLICATION_SETTINGS)
            )

            GuidedFixAction.BATTERY_OPTIMIZATION -> listOf(
                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
                Intent(Settings.ACTION_APPLICATION_SETTINGS)
            )
        }

        for (intent in candidates) {
            val opened = runCatching {
                activity.startActivity(intent)
                true
            }.getOrDefault(false)
            if (opened) return GuidedNavigationResult.Opened(plan.instructions)
        }

        return GuidedNavigationResult.Unavailable
    }

    private fun appDetails(packageName: String?): Intent =
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireNotNull(packageName), null)
        )
}
