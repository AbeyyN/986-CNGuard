# Physical China-ROM release gate

**Current status: UNVERIFIED.** As of the creation of this checklist no Xiaomi China-ROM phone is connected to the trusted 986 host and no owner-supplied test evidence has been accepted. CI build success is not a device test.

## Prerequisites

- A test Xiaomi/Redmi/POCO phone that really runs a stock China-ROM build, and the owner's consent to perform manual tests.
- Exact CN Guard candidate build and checksum.
- Capture only the phone model, Android version, HyperOS build, Security Center/PowerKeeper version when accessible, Google Play services version and diagnostic outcomes. Redact all serial numbers, account identifiers, IPs, phone numbers, notification bodies and pairing codes.
- Use an isolated test scenario for app notifications; do not share financial, authentication or OTP notifications.

## Required manual verification record

| Test ID | Procedure | Pass criterion | Evidence state |
| --- | --- | --- | --- |
| CN-01 | Install and open the candidate with Wireless Debugging off | Standard diagnostic UI works; no crash or privileged dependency | NOT TESTED |
| CN-02 | Test device-local alert, screen on | User visually confirms posted notification and whether heads-up is shown | NOT TESTED |
| CN-03 | Repeat alert, screen off/locked | User observes expected system notification behavior | NOT TESTED |
| CN-04 | Turn on Wireless Debugging and show pairing code locally | CN Guard discovers only its *own* pairing endpoint; never paste codes into report | NOT TESTED |
| CN-05 | Complete in-app pairing | Authenticated connection succeeds on the exact phone/ROM | NOT TESTED |
| CN-06 | Run read-only Greezer and FCM-socket diagnostics | Actual probe result captured; access denied must display UNKNOWN, not PASS | NOT TESTED |
| CN-07 | Turn Wireless Debugging off, close/reopen CN Guard | Standard Mode remains fully functional without debugging enabled | NOT TESTED |
| CN-08 | Re-enable Wireless Debugging; reconnect if supported | Trusted re-connect behavior recorded without claiming permanent privilege | NOT TESTED |
| CN-09 | Wi-Fi then mobile network Google TCP probe | Separate results recorded; TCP reachability is not FCM delivery | NOT TESTED |
| CN-10 | Normal banking-sensitive daily workflow, debugging off | User confirms their selected banking app continues opening normally; no credentials/screenshots shared | NOT TESTED |
| CN-11 | Actual remote FCM message with screen on/off and user consent | End-to-end push measured separately from local alert and TCP probe | NOT IMPLEMENTED |
| CN-12 | Xiaomi state-changing fix and rollback | Original value captured, modification independently verified, original restored | DISABLED |

## Evidence acceptance

For each completed test, record only PASS/FAIL/BLOCKED with the **exact tested APK checksum**, device model, ROM build and a brief redacted observation in the private canonical HANDOFF.md or a sanitized support-matrix update. A screen capture containing an OTP or bank content is unacceptable. No untested device belongs in the verified support matrix.

Stable release is blocked until the required tests for the **chosen stable scope** pass. Diagnostic-only release scope may explicitly exclude CN-11 and CN-12 rather than pretending those features work. Firmware-specific claims are always scoped to the builds actually tested.
