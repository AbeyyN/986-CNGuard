package com.abeyytechxy.cnguard.notification

object NotificationEventStore {
    private const val MAX_EVENTS = 50
    private val lock = Any()
    private val events = ArrayDeque<NotificationMetadata>(MAX_EVENTS)

    fun add(event: NotificationMetadata) {
        synchronized(lock) {
            events.addFirst(event)
            while (events.size > MAX_EVENTS) {
                events.removeLast()
            }
        }
    }

    fun snapshot(limit: Int = MAX_EVENTS): List<NotificationMetadata> {
        val boundedLimit = limit.coerceIn(0, MAX_EVENTS)
        return synchronized(lock) {
            events.take(boundedLimit)
        }
    }

    fun size(): Int = synchronized(lock) { events.size }

    fun clear() {
        synchronized(lock) {
            events.clear()
        }
    }
}
