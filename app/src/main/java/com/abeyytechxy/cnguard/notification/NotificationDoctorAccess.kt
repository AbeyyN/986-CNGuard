package com.abeyytechxy.cnguard.notification

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
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

    fun notificationListenerSettingsIntent(): Intent =
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)

}
