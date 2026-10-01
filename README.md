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
- Make Shizuku and root optional rather than prerequisites.

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

## Capability levels

```text
Standard
    |
Enhanced Android access
    |
Local Bridge
    |
Shizuku / root fallback
```

Local Bridge is the preferred advanced path. It is intended to provide bounded Wireless ADB diagnostics and repair actions from inside CN Guard without requiring Termux or Shizuku. It is not a general-purpose shell.

## Initial roadmap

1. Device and ROM capability fingerprint
2. Google Play services and FCM diagnostics
3. Xiaomi background-control probes
4. Per-app notification and execution diagnostics
5. Notification channel and heads-up diagnostics
6. Verified safe-fix flows
7. Local Bridge discovery, pairing, and typed repair actions
8. Optional Shizuku and root adapters
9. Malaysia-focused compatibility profiles
10. Push latency test lab
11. Signed compatibility rule packs

## Safety model

The core application uses the least privilege required for diagnostics. Privileged operations are explicit and isolated behind adapters.

Local Bridge accepts predefined typed actions only. Remote compatibility data must not provide arbitrary command execution. Any future rule-pack system will be restricted to predefined actions, versioned, signed, and rollbackable.

## Development

See [WORKFLOW.md](WORKFLOW.md), [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md), and [docs/LOCAL_BRIDGE.md](docs/LOCAL_BRIDGE.md).

## License

License selection is pending before the first public release.
