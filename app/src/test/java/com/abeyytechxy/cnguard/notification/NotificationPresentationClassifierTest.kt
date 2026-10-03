package com.abeyytechxy.cnguard.notification

import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationPresentationClassifierTest {
    @Test
    fun suspendedNotificationNeedsAttention() {
        val result = NotificationPresentationClassifier.classify(
            importance = 4,
            suspended = true,
            suppressedVisualEffects = 0
        )

        assertEquals(PresentationState.ATTENTION, result.state)
    }

    @Test
    fun lowImportanceNeedsAttention() {
        val result = NotificationPresentationClassifier.classify(
            importance = 2,
            suspended = false,
            suppressedVisualEffects = 0
        )

        assertEquals(PresentationState.ATTENTION, result.state)
    }

    @Test
    fun highImportanceWithoutSuppressionDoesNotProveHeadsUp() {
        val result = NotificationPresentationClassifier.classify(
            importance = 4,
            suspended = false,
            suppressedVisualEffects = 0
        )

        assertEquals(PresentationState.INFO, result.state)
    }

    @Test
    fun highImportanceWithSuppressionNeedsAttention() {
        val result = NotificationPresentationClassifier.classify(
            importance = 4,
            suspended = false,
            suppressedVisualEffects = 1
        )

        assertEquals(PresentationState.ATTENTION, result.state)
    }

    @Test
    fun defaultImportanceIsInformational() {
        val result = NotificationPresentationClassifier.classify(
            importance = 3,
            suspended = false,
            suppressedVisualEffects = 0
        )

        assertEquals(PresentationState.INFO, result.state)
    }
}
