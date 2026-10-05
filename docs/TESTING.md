# Testing

Support claims are evidence-based. A successful build is not equivalent to verified device compatibility.

## CI gate

Every pull request that changes application code must pass:

- unit tests;
- debug APK assembly;
- Android lint.

Release candidates add release-variant build and signing verification.

## CN-ROM simulation harness

Before physical-device testing, run the fixture-driven harness documented in [CN-ROM simulation harness](CN_ROM_SIMULATION.md). It exercises production classifiers and parsers across healthy, restricted and inaccessible-evidence scenarios.

Simulation results are regression evidence only. They must never create a Verified support-matrix entry or establish ROM region.

## Physical-device record

A physical verification record should include:

- device model;
- Android version;
- HyperOS version and build;
- Security Center version when relevant;
- PowerKeeper version when relevant;
- Google Play services version when relevant;
- Local Bridge discovery result;
- pairing and authenticated-connect result when tested;
- diagnostic probe results;
- repair result and rollback result when a state-changing operation is tested.

Do not include IMEI, serial number, phone number, account identifiers or notification contents.

## Local Bridge test sequence

1. Confirm the device is the intended local test target.
2. Enable Developer Options and Wireless Debugging.
3. Open the Android pairing-code screen.
4. Confirm CN Guard discovers a pairing endpoint on the phone itself.
5. Pair using the temporary six-digit code.
6. Confirm the connect endpoint is rediscovered.
7. Run the fixed connection test.
8. Run read-only probes.
9. Disable Wireless Debugging.
10. Confirm normal CN Guard diagnostics continue to work.
11. Re-enable Wireless Debugging and confirm trusted reconnect behavior where the ROM supports it.

## Notification test sequence

Notification diagnostics should be tested separately for:

- screen on;
- screen off;
- lock screen;
- battery saver;
- Wi-Fi;
- mobile data;
- idle/Doze where practical.

Transport delivery and visual presentation are recorded as separate outcomes.

## Repair test sequence

A state-changing repair cannot be marked verified without:

1. capturing the original state;
2. reproducing the unhealthy state;
3. applying the smallest intended change;
4. verifying the target state;
5. verifying the user-visible outcome;
6. restoring the original state;
7. verifying rollback.

## Support matrix

Only results backed by the process above belong in [SUPPORT_MATRIX.md](SUPPORT_MATRIX.md) as **Verified**.
