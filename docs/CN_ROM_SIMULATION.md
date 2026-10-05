# CN-ROM simulation harness

The simulation harness exercises CN Guard policy and parser boundaries without pretending that an emulator or fixture is a verified Xiaomi China-ROM phone.

## What it covers

The fixture matrix can model:

- Xiaomi / Redmi / POCO family signals;
- Xiaomi vendor-package visibility present, absent, or indeterminate;
- Google endpoint DNS and TCP reachability outcomes;
- established Google Play services FCM socket evidence;
- unreadable socket evidence, which must remain UNKNOWN;
- notification importance, suspension and visual-effect suppression;
- Wireless ADB pairing/connect endpoint resolution;
- six-digit pairing-code validation.

The harness intentionally composes the same production classifiers and parsers used by the app.

## What it never proves

A passing simulation does not verify:

- a China-ROM build or region;
- Xiaomi PowerKeeper/Greezer runtime behavior;
- real screen-off or lock-screen presentation;
- Wireless ADB behavior on a specific Xiaomi firmware;
- remote FCM delivery or latency;
- reboot persistence;
- mobile-data behavior;
- banking-app compatibility.

romRegion is therefore forced to UNKNOWN for every synthetic fixture.

## Run

From a configured Android build environment:

    gradle :app:testDebugUnitTest

The normal CI and release-readiness gates remain required. Physical Xiaomi evidence remains the only route to a Verified support-matrix entry.
