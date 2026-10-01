# Development Workflow

This repository follows a diagnostic-first workflow. Public support claims are based on observed behavior, not ROM version assumptions.

## 1. Research and reproduce

- Record the device model, Android version, HyperOS build, and relevant Xiaomi component versions.
- Reproduce the problem before applying a change.
- Identify the failure layer: transport, Xiaomi process control, app execution, or notification presentation.
- Prefer runtime capability checks over version-only branching.

## 2. Probe before modify

Every repair feature starts as a read-only probe.

A probe must:

- return a bounded result such as available, unavailable, attention, or unknown;
- fail safely when Xiaomi changes an internal API;
- avoid hidden writes;
- keep diagnostic output free of personal notification content and account data.

## 3. Least-privilege implementation

The default application path stays unprivileged whenever possible.

Privileged behavior must be isolated behind an adapter and must not be required merely to open or diagnose the app. Shizuku and root support, if added, remain explicit opt-in capabilities.

## 4. Change contract

A state-changing operation follows this contract:

```text
preflight -> capture current state -> apply -> verify -> record result
                                      \
                                       -> rollback on failure
```

A successful API call alone is not verification.

## 5. Testing

Changes should cover, where applicable:

- unit tests for parsing and policy logic;
- Android lint;
- build verification;
- emulator or framework-level tests;
- physical Xiaomi device verification;
- screen-on and screen-off behavior;
- reboot recovery;
- regression against previously verified builds.

## 6. Compatibility evidence

A ROM or device is only marked supported after physical verification.

Use `docs/SUPPORT_MATRIX.md` to record:

- device;
- ROM/build;
- Android version;
- probe results;
- repair result;
- known limitations.

Untested behavior must be labelled experimental or unknown.

## 7. Branch and release flow

- `main` is the integration baseline.
- Non-trivial work should use focused feature branches.
- CI must pass before release candidates are tagged.
- Public releases require a reproducible build path, stable signing, checksum publication, and rollback notes.
- Debug artifacts are not production releases.

## 8. Rule-pack changes

Any future remote compatibility rules must be:

- schema-constrained;
- cryptographically signed;
- versioned;
- rollbackable;
- limited to predefined operations.

Remote rules must never provide arbitrary shell execution.

## 9. Security and privacy

Do not collect notification contents, banking data, credentials, IMEI, Android ID, phone numbers, or account identifiers for diagnostics.

Diagnostic uploads, if introduced, must be opt-in and minimize device-identifying data.
