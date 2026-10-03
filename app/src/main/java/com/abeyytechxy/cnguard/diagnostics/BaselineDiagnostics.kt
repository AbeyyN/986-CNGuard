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
            add(deviceVendor(context))
            add(romRegionProbe())
            add(packageProbe(context, GMS_PACKAGE, "Google Play services"))
            add(packageProbe(context, SECURITY_CENTER_PACKAGE, "Xiaomi Security Center"))
            add(packageProbe(context, POWERKEEPER_PACKAGE, "Xiaomi PowerKeeper"))
            add(milletProbe(context))
        }

        val deviceSummary = listOf(
            Build.MANUFACTURER,
            Build.BRAND,
            Build.MODEL,
            "Android ${Build.VERSION.RELEASE}",
            "API ${Build.VERSION.SDK_INT}"
        ).joinToString(" · ")

        return DiagnosticReport(
            deviceSummary = deviceSummary,
            items = items
        )
    }

    private fun deviceVendor(context: Context): DiagnosticItem {
        val family = DeviceFamilyClassifier.classify(
            manufacturer = Build.MANUFACTURER,
            brand = Build.BRAND,
            securityCenterPresent = packageVisible(context, SECURITY_CENTER_PACKAGE),
            powerKeeperPresent = packageVisible(context, POWERKEEPER_PACKAGE)
        )

        return when (family) {
            XiaomiFamilyState.XIAOMI_FAMILY -> DiagnosticItem(
                id = "device.family",
                state = DiagnosticState.PASS,
                title = "Xiaomi / Redmi / POCO family signal",
                summary = "Hardware or vendor-package signals match Xiaomi family",
                detail = "ROM region is not established by this classification"
            )
            XiaomiFamilyState.UNCONFIRMED -> DiagnosticItem(
                id = "device.family",
                state = DiagnosticState.UNKNOWN,
                title = "Device family",
                summary = "Xiaomi family could not be confirmed"
            )
            XiaomiFamilyState.OTHER_MANUFACTURER -> DiagnosticItem(
                id = "device.family",
                state = DiagnosticState.INFO,
                title = "Non-Xiaomi manufacturer",
                summary = "General Android notification diagnostics remain available"
            )
        }
    }

    private fun romRegionProbe(): DiagnosticItem = DiagnosticItem(
        id = "rom.region",
        state = DiagnosticState.UNKNOWN,
        title = "China-ROM verification",
        summary = "Not established by manufacturer, language, or a package name",
        detail = "Only device/build-specific verification may establish ROM region"
    )

    private fun packageVisible(context: Context, packageName: String): Boolean? =
        try {
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                context.packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (_: Exception) {
            null
        }

    private fun packageProbe(
        context: Context,
        packageName: String,
        label: String
    ): DiagnosticItem {
        val installed = try {
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                context.packageManager.getPackageInfo(packageName, 0)
            }
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
