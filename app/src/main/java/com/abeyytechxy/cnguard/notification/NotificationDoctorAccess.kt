package com.abeyytechxy.cnguard.notification

import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings

class NotificationDoctorAccess(
    private val context: Context
) {
    private val appContext = context.applicationContext

    fun hasNotificationListenerAccess(): Boolean {
        val manager = appContext.getSystemService(NotificationManager::class.java)
        val component = ComponentName(
            appContext,
            NotificationMetadataListenerService::class.java
        )
        return manager.isNotificationListenerAccessGranted(component)
    }

    fun hasUsageAccess(): Boolean {
        val appOps = appContext.getSystemService(AppOpsManager::class.java)
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            appContext.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun notificationListenerSettingsIntent(): Intent =
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)

    fun usageAccessSettingsIntent(): Intent =
        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
}
