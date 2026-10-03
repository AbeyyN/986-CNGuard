package com.abeyytechxy.cnguard.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

enum class LocalAlertState {
    APP_NOTIFICATIONS_DISABLED,
    CHANNEL_BLOCKED,
    CHANNEL_NOT_HIGH_IMPORTANCE,
    ELIGIBLE_FOR_HIGH_PRIORITY_ALERT,
    UNAVAILABLE
}

data class LocalAlertResult(
    val state: LocalAlertState,
    val summary: String
)

object LocalAlertPolicy {
    fun assess(appNotificationsEnabled: Boolean, channelImportance: Int?): LocalAlertResult {
        if (!appNotificationsEnabled) {
            return LocalAlertResult(
                LocalAlertState.APP_NOTIFICATIONS_DISABLED,
                "CN Guard notifications are disabled. No test notification sent."
            )
        }

        if (channelImportance == null) {
            return LocalAlertResult(
                LocalAlertState.UNAVAILABLE,
                "Test channel state unavailable; the local alert was not sent."
            )
        }

        if (channelImportance == 0) {
            return LocalAlertResult(
                LocalAlertState.CHANNEL_BLOCKED,
                "The test notification channel is blocked. No test notification sent."
            )
        }

        if (channelImportance < 4) {
            return LocalAlertResult(
                LocalAlertState.CHANNEL_NOT_HIGH_IMPORTANCE,
                "Test channel importance is below HIGH. A visible popup is not expected."
            )
        }

        return LocalAlertResult(
            LocalAlertState.ELIGIBLE_FOR_HIGH_PRIORITY_ALERT,
            "High-priority channel is available. Visual popup remains unverified."
        )
    }
}

class LocalAlertLab(context: Context) {
    companion object {
        private const val CHANNEL_ID = "cnguard_local_alert_test"
        private const val NOTIFICATION_ID = 986
    }

    private val appContext = context.applicationContext
    private val manager = appContext.getSystemService(NotificationManager::class.java)

    fun send(): LocalAlertResult {
        return try {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "CN Guard local alert test",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Optional device-local presentation test. Not an FCM push test."
                }
            )

            val result = LocalAlertPolicy.assess(
                manager.areNotificationsEnabled(),
                manager.getNotificationChannel(CHANNEL_ID)?.importance
            )

            if (result.state == LocalAlertState.APP_NOTIFICATIONS_DISABLED ||
                result.state == LocalAlertState.CHANNEL_BLOCKED ||
                result.state == LocalAlertState.UNAVAILABLE
            ) {
                return result
            }

            val notification = Notification.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("CN Guard local alert test")
                .setContentText("Device-local notification only. Remote FCM is not tested.")
                .setCategory(Notification.CATEGORY_STATUS)
                .setAutoCancel(true)
                .build()

            manager.notify(NOTIFICATION_ID, notification)

            LocalAlertResult(
                state = result.state,
                summary = result.summary +
                    " Android accepted the local notification request. " +
                    "Confirm its visibility manually; this does not measure FCM delivery."
            )
        } catch (_: SecurityException) {
            LocalAlertResult(
                LocalAlertState.APP_NOTIFICATIONS_DISABLED,
                "Notification permission is unavailable; nothing was posted."
            )
        } catch (_: RuntimeException) {
            LocalAlertResult(
                LocalAlertState.UNAVAILABLE,
                "Local notification test failed on this device; no result is verified."
            )
        }
    }
}
