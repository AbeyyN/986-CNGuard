package com.abeyytechxy.cnguard.notification

data class NotificationMetadata(
    val packageName: String,
    val postedAtMillis: Long,
    val observedAtMillis: Long,
    val importance: Int,
    val channelImportance: Int?,
    val suspended: Boolean,
    val suppressedVisualEffects: Int,
    val screenInteractive: Boolean,
    val keyguardLocked: Boolean,
    val powerSaveMode: Boolean
) {
    val observationDelayMillis: Long
        get() = (observedAtMillis - postedAtMillis).coerceAtLeast(0L)
}
