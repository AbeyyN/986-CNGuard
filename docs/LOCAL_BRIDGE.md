# Local Bridge

Local Bridge is the primary advanced diagnostics path for 986 CN Guard. It provides optional Wireless ADB capabilities without requiring Termux or Shizuku as a runtime dependency.

## Product model

The application has four capability levels:

1. **Standard** — diagnostics and guided fixes using normal Android APIs.
2. **Enhanced** — user-granted Android special access for deeper diagnostics.
3. **Local Bridge** — built-in Wireless ADB pairing and bounded diagnostic sessions.
4. **Expert** — optional Shizuku or root adapters for capabilities that cannot be provided safely by the preceding levels.

Shizuku is not required for normal use.

## Local Bridge lifecycle

```text
open Developer Options
        |
enable Wireless debugging
        |
open "Pair device with pairing code"
        |
CN Guard discovers the local pairing endpoint
        |
enter the 6-digit code
        |
CN Guard stores its ADB host identity in app-private storage
        |
discover the trusted connect endpoint
        |
run predefined read-only diagnostics
        |
end the session
        |
Wireless debugging may be disabled again
```

The design does not require Wireless debugging to remain enabled during normal daily use.

## Discovery boundary

Android Wireless ADB advertises:

- `_adb-tls-pairing._tcp`
- `_adb-tls-connect._tcp`

CN Guard resolves those services through Android NSD and accepts only endpoints whose resolved IP belongs to a network interface on the current phone. This prevents the local bridge flow from silently selecting a different Android device advertising ADB on the same LAN.

## Pairing transport

The transport adapter uses Kadb for the ADB wire protocol and pairing flow. CN Guard persists only its private ADB host identity in the app's private files directory. Pairing codes are not persisted.

The APK currently uses Kadb 2.1.4. Its Android pairing path includes a GPL-3.0 SPAKE2 dependency, so release licensing must remain GPL-compatible.

Bouncy Castle is explicitly pinned above the vulnerable 1.84 version declared by Kadb 2.1.4.

## Security boundary

Local Bridge is not a general-purpose terminal.

There is no UI field for arbitrary shell commands. Shell operations are fixed inside typed application features. Remote rule packs, downloaded configuration, and network responses cannot supply shell command strings.

Current Local Bridge shell access is read-only:

- a fixed connection echo;
- Greezer service visibility;
- Google Play services UID lookup;
- read-only TCP socket-table inspection for FCM ports 5228–5230.

State-changing repair commands remain out of scope until physical-device verification and rollback behavior are established.

## Verification states

Build success does not prove physical Wireless ADB behavior.

A release can only claim pairing or Xiaomi ROM support after testing on the exact device/ROM family and recording it in `SUPPORT_MATRIX.md`.

## Future Android local-network changes

Local-network access is becoming more explicitly permissioned on newer Android releases. Local Bridge must adapt to the platform's user-approved discovery model as target SDK levels advance.
