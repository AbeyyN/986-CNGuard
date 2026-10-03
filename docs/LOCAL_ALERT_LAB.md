# Local Alert Lab

The Local Alert Lab is a manually triggered, entirely device-local notification test.

It requests the Android POST_NOTIFICATIONS runtime permission only when the user presses **Send Local Test Alert** on Android 13 and later. No permission request occurs at normal app startup.

The app creates a fixed, high-importance test channel. Android users can change that channel's importance or block CN Guard notifications independently; the test inspects the current state rather than assuming the originally requested importance is still active.

If permitted, the lab submits a fixed local test notification to Android. A successful submission does **not** prove:
- a heads-up popup was shown;
- another application's notification configuration is healthy;
- Google Play services has a working FCM transport;
- remote push latency is within any target.

The user must personally check whether the test notification appeared and whether it produced the desired presentation.

The test has no remote service, Firebase token, telemetry collection or notification content ingestion. An actual remote FCM latency lab will require a distinct architecture and explicit consent.
