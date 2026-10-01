# 986 CN Guard

986 CN Guard is an Android diagnostics and compatibility project for Xiaomi China-ROM devices used outside China.

The project focuses on notification reliability, Google/Firebase push transport, Xiaomi background execution controls, notification presentation, and related compatibility problems that can affect daily-use and banking applications.

> **Status:** pre-alpha. The repository is under active development and does not yet provide a production-ready repair mode.

## Goals

- Diagnose the actual failure layer before changing device settings.
- Keep the default app path low-privilege and banking-friendly.
- Detect Xiaomi capabilities instead of hard-coding one HyperOS version.
- Separate FCM transport health from per-app execution and notification presentation.
- Verify every repair and keep a rollback path.
- Support optional privileged adapters without making them a requirement for the core app.

## Architecture direction

The project is capability-first:

```text
device probe
    |
    +-- Android / ROM capabilities
    +-- Google Play services / FCM
    +-- Xiaomi PowerKeeper / Greezer / Aurogon
    +-- per-app execution state
    +-- notification presentation
            |
            v
      diagnostic report
            |
            v
      adapter + verified action
```

HyperOS and Android version numbers are useful signals, but they are not treated as the only source of truth. Feature availability is determined by runtime probes and adapter support.

## Initial roadmap

1. Device and ROM capability fingerprint
2. Google Play services and FCM diagnostics
3. Xiaomi background-control probes
4. Per-app notification and execution diagnostics
5. Notification channel and heads-up diagnostics
6. Verified safe-fix flows
7. Optional Shizuku and root adapters
8. Malaysia-focused compatibility profiles
9. Push latency test lab
10. Signed compatibility rule packs

## Safety model

The core application will use the least privilege required for diagnostics. Privileged operations, when added, will be explicit and isolated behind separate adapters.

Remote compatibility data must not provide arbitrary command execution. Any future rule-pack system will be restricted to predefined actions, versioned, signed, and rollbackable.

## Development

See [WORKFLOW.md](WORKFLOW.md) and [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) once the initial scaffold is in place.

## License

License selection is pending before the first public release.
