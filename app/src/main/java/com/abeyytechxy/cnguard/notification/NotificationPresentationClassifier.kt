package com.abeyytechxy.cnguard.notification

enum class PresentationState {
    HEALTHY,
    ATTENTION,
    INFO
}

data class PresentationAssessment(
    val state: PresentationState,
    val summary: String
)

object NotificationPresentationClassifier {
    private const val IMPORTANCE_NONE = 0
    private const val IMPORTANCE_MIN = 1
    private const val IMPORTANCE_LOW = 2
    private const val IMPORTANCE_DEFAULT = 3
    private const val IMPORTANCE_HIGH = 4

    fun classify(
        importance: Int,
        suspended: Boolean,
        suppressedVisualEffects: Int
    ): PresentationAssessment {
        if (suspended) {
            return PresentationAssessment(
                PresentationState.ATTENTION,
                "Notification delivered but package notifications are suspended"
            )
        }

        if (importance in IMPORTANCE_NONE..IMPORTANCE_LOW) {
            return PresentationAssessment(
                PresentationState.ATTENTION,
                "Notification delivered with low or disabled alert importance"
            )
        }

        if (importance >= IMPORTANCE_HIGH && suppressedVisualEffects != 0) {
            return PresentationAssessment(
                PresentationState.ATTENTION,
                "High-importance notification delivered while visual effects are suppressed"
            )
        }

        if (importance >= IMPORTANCE_HIGH) {
            return PresentationAssessment(
                PresentationState.HEALTHY,
                "High-importance notification delivery observed"
            )
        }

        if (importance == IMPORTANCE_DEFAULT) {
            return PresentationAssessment(
                PresentationState.INFO,
                "Notification delivery observed at default importance"
            )
        }

        return PresentationAssessment(
            PresentationState.INFO,
            "Notification delivery observed; ranking importance unavailable"
        )
    }
}
