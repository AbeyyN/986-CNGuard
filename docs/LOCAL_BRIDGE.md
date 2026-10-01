# Local Bridge

Local Bridge is the primary advanced diagnostics path for 986 CN Guard. It is designed to provide optional Wireless ADB capabilities without requiring Termux or Shizuku as a runtime dependency.

## Product model

The application has four capability levels:

1. **Standard** — diagnostics and guided fixes using normal Android APIs.
2. **Enhanced** — user-granted Android special access for deeper diagnostics.
3. **Local Bridge** — a built-in Wireless ADB client for bounded advanced diagnostic sessions.
4. **Expert** — optional Shizuku or root adapters for capabilities that cannot be provided safely by the preceding levels.

Shizuku is not required for normal use.

## Local Bridge lifecycle

The intended lifecycle is:

```text
open Developer Options
        |
enable Wireless debugging
        |
pair CN Guard once
        |
discover the trusted ADB service
        |
run a predefined diagnostic probe
        |
record and verify the result
        |
stop the bridge session
        |
user may disable Wireless debugging again
```

A banking-sensitive user should not need to leave Wireless debugging enabled during normal daily use.

## Discovery

Android Wireless ADB advertises two relevant mDNS service types:

- `_adb-tls-pairing._tcp.`
- `_adb-tls-connect._tcp.`

The bootstrap implementation uses Android Network Service Discovery to detect those services. It does not pair, authenticate, or execute shell operations yet.

## Security boundary

Local Bridge is not a general-purpose terminal.

Privileged capabilities are represented by predefined typed operations. Arbitrary command strings from UI input, remote rule packs, network responses, or downloaded configuration are not accepted.

The first bootstrap exposes diagnostic probe identifiers only. State-changing operations remain out of scope until pairing, transport, physical-device behavior, verification, and rollback semantics have been validated.

## Initial diagnostic probes

The initial catalog reserves typed identifiers for:

- reading the Greezer state relevant to Google Play services;
- reading FCM connection state;
- reading Xiaomi Autostart state.

The current bootstrap does not execute those probes yet. Availability remains capability-driven.

## Pairing and transport

Pairing and ADB TLS transport are separate from discovery and are not considered implemented until they pass physical-device verification.

The project will not copy a third-party terminal application into the codebase. External implementation details must be reviewed for licensing and provenance before code is adopted.

## Future Android local-network changes

Local-network access is becoming more explicitly permissioned on newer Android releases. Local Bridge must adapt to the platform's user-approved service discovery model where required rather than assuming unrestricted LAN access indefinitely.
