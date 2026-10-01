package com.abeyytechxy.cnguard.diagnostics

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings

object BaselineDiagnostics {
    private const val GMS_PACKAGE = "com.google.android.gms"
    private const val SECURITY_CENTER_PACKAGE = "com.miui.securitycenter"
    private const val POWERKEEPER_PACKAGE = "com.miui.powerkeeper"
    private const val MILLET_NO_RESTRICT_APP = "MILLET_NO_RESTRICT_APP"

    fun collect(context: Context): DiagnosticReport {
        val items = buildList {
            add(deviceVendor())
            add(packageProbe(context, GMS_PACKAGE, "Google Play services"))
            add(packageProbe(context, SECURITY_CENTER_PACKAGE, "Xiaomi Security Center"))
            add(packageProbe(context, POWERKEEPER_PACKAGE, "Xiaomi PowerKeeper"))
            add(milletProbe(context))
        }

        val deviceSummary = listOf(
            Build.MANUFACTURER,
            Build.MODEL,
            "Android ${Build.VERSION.RELEASE}",
            "API ${Build.VERSION.SDK_INT}"
        ).joinToString(" · ")

        return DiagnosticReport(
            deviceSummary = deviceSummary,
            items = items
        )
    }

    private fun deviceVendor(): DiagnosticItem {
        val isXiaomi = Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)
        return DiagnosticItem(
            id = "device.vendor",
            state = if (isXiaomi) DiagnosticState.PASS else DiagnosticState.UNSUPPORTED,
            title = "Xiaomi device",
            summary = if (isXiaomi) {
                "Xiaomi manufacturer detected"
            } else {
                "This baseline targets Xiaomi firmware behavior"
            },
            detail = Build.MANUFACTURER
        )
    }

    private fun packageProbe(
        context: Context,
        packageName: String,
        label: String
    ): DiagnosticItem {
        val installed = try {
            context.packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (_: Throwable) {
            return DiagnosticItem(
                id = "package.$packageName",
                state = DiagnosticState.UNKNOWN,
                title = label,
                summary = "Package state could not be determined"
            )
        }

        return DiagnosticItem(
            id = "package.$packageName",
            state = if (installed) DiagnosticState.PASS else DiagnosticState.ATTENTION,
            title = label,
            summary = if (installed) "Installed" else "Not detected",
            detail = packageName
        )
    }

    private fun milletProbe(context: Context): DiagnosticItem {
        val value = try {
            Settings.System.getString(
                context.contentResolver,
                MILLET_NO_RESTRICT_APP
            )
        } catch (_: Throwable) {
            null
        }

        if (value.isNullOrBlank()) {
            return DiagnosticItem(
                id = "xiaomi.millet_no_restrict_app",
                state = DiagnosticState.UNKNOWN,
                title = "Xiaomi no-restrict list",
                summary = "Setting is absent, empty, or not readable",
                detail = MILLET_NO_RESTRICT_APP
            )
        }

        val packages = value
            .split(',')
            .map(String::trim)
            .filter(String::isNotEmpty)
            .toSet()

        val gmsPresent = GMS_PACKAGE in packages

        return DiagnosticItem(
            id = "xiaomi.millet_no_restrict_app",
            state = if (gmsPresent) DiagnosticState.PASS else DiagnosticState.ATTENTION,
            title = "Xiaomi no-restrict list",
            summary = if (gmsPresent) {
                "Google Play services is present"
            } else {
                "Google Play services is not present"
            },
            detail = "${packages.size} entries detected"
        )
    }
}
