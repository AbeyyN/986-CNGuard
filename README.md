# 986 CN Guard

986 CN Guard is an Android diagnostics and compatibility project for Xiaomi China-ROM devices used outside China.

The project focuses on notification reliability, Google/Firebase push transport, Xiaomi background execution controls, notification presentation, and related compatibility problems that can affect daily-use and banking applications.

> **Status:** alpha development. Diagnostic features are usable for testing, but Xiaomi-specific repair support is not yet declared stable.

## Goals

- Diagnose the actual failure layer before changing device settings.
- Keep the default app path low-privilege and banking-friendly.
- Detect Xiaomi capabilities instead of hard-coding one HyperOS version.
- Separate FCM transport health from per-app execution and notification presentation.
- Verify every repair and keep a rollback path.
- Make Shizuku and root optional rather than prerequisites.

## Capability levels

```text
Standard diagnostics
        |
Enhanced Android access
        |
Local Bridge
        |
Shizuku / root fallback
```

Local Bridge is the preferred advanced path. It pairs with Android Wireless Debugging directly inside CN Guard and does not require Termux or Shizuku. The bridge is intentionally bounded; CN Guard does not expose a general-purpose ADB terminal.

## Current Local Bridge scope

- discover the current phone's ADB pairing and connect services;
- pair with a six-digit Wireless Debugging code;
- persist the CN Guard ADB host identity in app-private storage;
- verify an authenticated ADB connection;
- inspect Greezer service visibility;
- inspect whether Google Play services has an established FCM socket on ports 5228–5230.

Repair commands remain disabled until physical Xiaomi CN-ROM verification is complete.

## Initial roadmap

1. Device and ROM capability fingerprint
2. Google Play services and FCM diagnostics
3. Xiaomi background-control probes
4. Per-app notification and execution diagnostics
5. Notification channel and heads-up diagnostics
6. Verified safe-fix flows
7. Local Bridge pairing and typed diagnostics
8. Verified Local Bridge repair actions
9. Optional Shizuku and root adapters
10. Malaysia-focused compatibility profiles
11. Push latency test lab
12. Signed compatibility rule packs

## Optional Local Alert Lab

A manual local-notification test can check whether Android accepts a CN Guard notification and inspect the test channel importance. The POST_NOTIFICATIONS permission is requested only when that test is started. This is deliberately **not** an FCM push latency test and cannot establish whether another app receives push messages.

Usage Access is not requested by the current build because no implemented diagnostic consumes UsageStats data.

## Optional Google endpoint reachability test

A manual test checks ordinary TCP connectivity from the current device network to documented Google FCM endpoints. A successful socket connection is not proof that a push notification was delivered. See [FCM network probe](docs/FCM_NETWORK_PROBE.md).

## Safety model

The core application uses the least privilege required for diagnostics. Privileged operations are explicit and isolated behind adapters.

Local Bridge accepts predefined typed operations only. Remote compatibility data must not provide arbitrary command execution.

## Development

See [WORKFLOW.md](WORKFLOW.md), [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md), [docs/LOCAL_BRIDGE.md](docs/LOCAL_BRIDGE.md), and [docs/SUPPORT_MATRIX.md](docs/SUPPORT_MATRIX.md).

## License

Release licensing is GPL-compatible because the current Wireless ADB pairing transport includes a GPL-3.0 SPAKE2 dependency. A full license and third-party notice set will be included before the first tagged release.
