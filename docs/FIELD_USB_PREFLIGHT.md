# Read-only USB preflight for stock Xiaomi China ROM

Use `scripts/field-device-preflight.sh` **only after the device owner has deliberately connected and authorized one Xiaomi/Redmi/POCO phone through USB debugging**. It does not install software, activate debugging, change settings, root, pair or collect notification contents.

1. Unplug unrelated Android devices, particularly any Samsung or personal daily-use phone. Keep exactly one authorized Xiaomi test phone on USB.
2. Check that its ROM is **genuine stock Xiaomi China ROM** using the device's official About phone/build information. Region property strings alone are indicators, not proof; customized ROMs are not automatically accepted.
3. Obtain a reviewed candidate APK and its exact SHA-256 from the private release artifact manifest, not an arbitrary public download.
4. On the trusted test machine, with Android platform-tools and Android Build Tools available, run:

```bash
export CNGUARD_ADB_BIN=/path/to/platform-tools/adb
export CNGUARD_APKSIGNER_BIN=/path/to/build-tools/apksigner
export CNGUARD_EXPECTED_APK_SHA256=REPLACE_WITH_REVIEWED_64_CHAR_SHA256
bash scripts/field-device-preflight.sh /absolute/path/to/reviewed-candidate.apk
```

The script aborts on unknown APK signature, checksum mismatch, no authorized USB Xiaomi candidate or multiple attached USB devices. It **never** prints the USB serial, IMEI, IP, account, token or notification contents. Save only the model/Android/HyperOS markers and candidate checksum in the private field QA record.

**Preflight is not a physical-app PASS.** After successful preflight, explicitly obtain the test-device owner's consent **before installing** or opening the candidate, then execute the manual tests in [Physical China-ROM test plan](PHYSICAL_XIAOMI_TEST_PLAN.md). No scripts automatically mark support Verified, enable Wireless Debugging or perform state-changing repairs.
