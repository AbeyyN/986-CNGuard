package com.abeyytechxy.cnguard.notification

import android.app.KeyguardManager
import android.app.NotificationListenerService
import android.os.PowerManager
import android.service.notification.NotificationListenerService.Ranking
import android.service.notification.NotificationListenerService.RankingMap
import android.service.notification.StatusBarNotification

class NotificationMetadataListenerService : NotificationListenerService() {
    private val powerManager by lazy { getSystemService(PowerManager::class.java) }
    private val keyguardManager by lazy { getSystemService(KeyguardManager::class.java) }

    override fun onNotificationPosted(
        sbn: StatusBarNotification,
        rankingMap: RankingMap
    ) {
        if (sbn.packageName == packageName) {
            return
        }

        val ranking = Ranking()
        val ranked = rankingMap.getRanking(sbn.key, ranking)
        val observedAt = System.currentTimeMillis()

        NotificationEventStore.add(
            NotificationMetadata(
                packageName = sbn.packageName,
                postedAtMillis = sbn.postTime,
                observedAtMillis = observedAt,
                importance = if (ranked) ranking.importance else -1,
                channelImportance = if (ranked) ranking.channel?.importance else null,
                suspended = ranked && ranking.isSuspended,
                suppressedVisualEffects = if (ranked) ranking.suppressedVisualEffects else 0,
                screenInteractive = powerManager.isInteractive,
                keyguardLocked = keyguardManager.isKeyguardLocked,
                powerSaveMode = powerManager.isPowerSaveMode
            )
        )
    }
}
