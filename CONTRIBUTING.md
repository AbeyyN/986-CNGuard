# Contributing

Contributions should keep 986 CN Guard capability-driven, least-privilege and evidence-based.

## Before changing device state

New Xiaomi-specific mechanisms start as read-only probes. A state-changing action requires a documented preflight, verification and rollback contract before it is exposed to users.

## Pull requests

Keep changes focused. Include:

- the problem being addressed;
- the capability or API used;
- failure behavior when the capability is absent;
- tests for parsing or policy logic where practical;
- physical-device evidence for any compatibility claim.

Do not label an untested ROM or device as supported.

## Privileged code

Local Bridge, Shizuku and root adapters must not expose arbitrary command execution.

Package names, user IDs and other command parameters must be validated before a privileged operation is constructed. Remote rules may select supported operation identifiers but may not supply shell command text.

## Diagnostic data

Do not add notification contents, credentials, banking information, phone numbers or stable device identifiers to logs, tests, screenshots or fixtures.

## Public repository hygiene

Do not commit signing keys, credentials, local configuration, private operational documents, device identifiers or user data.
