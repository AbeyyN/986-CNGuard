# Security Policy

986 CN Guard interacts with device compatibility, notification and optional Wireless ADB features. Security-sensitive behavior is intentionally constrained.

## Supported versions

The project is pre-release. Security fixes are applied to the current development line until the first stable release defines a longer support policy.

## Reporting a vulnerability

Do not publish exploit details, credentials, device identifiers, notification contents or banking information in a public issue.

Use GitHub private vulnerability reporting when it is available for this repository. If private reporting is unavailable, open a public issue containing only enough information to request a private contact channel. Do not include proof-of-concept payloads or sensitive logs in that issue.

## Security boundaries

The following are project requirements:

- no arbitrary shell terminal in Local Bridge;
- no shell command strings supplied by remote rule packs, downloaded configuration or user text input;
- privileged operations must be predefined and parameter-validated;
- state-changing operations require preflight, state capture, verification and a rollback strategy;
- pairing codes are temporary input and must not be persisted;
- ADB host identity material stays in application-private storage;
- diagnostic output must not contain notification message bodies, credentials, banking data, phone numbers, IMEI, Android ID or account identifiers;
- physical-device support claims require recorded verification against the stated device and ROM build.

## Dependency policy

Dependencies that participate in cryptography, ADB transport or privileged execution are reviewed before release. Known vulnerable versions must not be intentionally shipped when a compatible fixed release is available.

## Release signing

Production APK signing material must never be committed to this repository. Stable releases must use a persistent signing identity so future APK updates remain installable over earlier releases.
