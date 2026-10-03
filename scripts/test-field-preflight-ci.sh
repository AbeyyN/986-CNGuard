#!/usr/bin/env bash
set -euo pipefail
tmp="$(mktemp -d)"
trap 'rm -rf -- "$tmp"' EXIT
root="$(cd "$(dirname "$0")/.." && pwd -P)"
printf 'fake signing data\n' > "$tmp/test.apk"
sha="$(sha256sum "$tmp/test.apk" | awk '{print $1}')"
pub="$(openssl x509 -in "$root/release/signing-certificate.pem" -fingerprint -sha256 -noout |
    sed 's/.*=//' | tr -d ':' | tr '[:upper:]' '[:lower:]')"
cat > "$tmp/apksigner" <<'SH'
#!/usr/bin/env bash
echo "Verifies"
echo "V3.0 Signer: certificate SHA-256 digest: $CNGUARD_TEST_CERT"
SH
cat > "$tmp/adb" <<'SH'
#!/usr/bin/env bash
if test "$1" = devices; then
    case "$CNGUARD_TEST_DEVICE" in
        xiaomi) printf 'List of devices attached\nSECRET123 device usb:1-1 model:Test\n' ;;
        samsung) printf 'List of devices attached\nSECRET123 device usb:1-1 model:Test\n' ;;
        multiple) printf 'List of devices attached\nSECRET123 device usb:1-1\nSECRET456 device usb:2-1\n' ;;
        *) printf 'List of devices attached\n' ;;
    esac
elif test "$1 $2" = '-d shell' && test "$3" = getprop; then
    case "$4" in
        ro.product.manufacturer) test "$CNGUARD_TEST_DEVICE" = samsung && echo Samsung || echo Xiaomi ;;
        ro.product.brand) test "$CNGUARD_TEST_DEVICE" = samsung && echo samsung || echo Redmi ;;
        ro.product.model) echo 'Mock test model' ;;
        ro.build.version.release) echo 16 ;;
        ro.build.version.sdk) echo 36 ;;
        ro.mi.os.version.name) echo OS3 ;;
        ro.miui.region) echo CN ;;
        ro.product.mod_device) echo mock_cn ;;
    esac
else
    echo UNEXPECTED_ADB_ACTION >&2
    exit 9
fi
SH
chmod +x "$tmp/adb" "$tmp/apksigner"
export CNGUARD_ADB_BIN="$tmp/adb"
export CNGUARD_APKSIGNER_BIN="$tmp/apksigner"
export CNGUARD_TEST_CERT="$pub"
export CNGUARD_EXPECTED_APK_SHA256="$sha"
export CNGUARD_TEST_DEVICE=xiaomi
bash "$root/scripts/field-device-preflight.sh" "$tmp/test.apk" > "$tmp/success"
grep -q 'NO_INSTALL_NO_WRITES_NO_NETWORK_PAIRING=PASS' "$tmp/success"
if grep -q SECRET "$tmp/success"; then echo 'USB SERIAL LEAKED' >&2; exit 1; fi
export CNGUARD_TEST_DEVICE=samsung
if bash "$root/scripts/field-device-preflight.sh" "$tmp/test.apk" > "$tmp/fail" 2>&1; then
    echo 'SAMSUNG ACCEPTED AS XIAOMI' >&2; exit 1
fi
export CNGUARD_TEST_DEVICE=multiple
if bash "$root/scripts/field-device-preflight.sh" "$tmp/test.apk" > "$tmp/fail" 2>&1; then
    echo 'AMBIGUOUS USB DEVICES ACCEPTED' >&2; exit 1
fi
export CNGUARD_TEST_DEVICE=xiaomi
export CNGUARD_TEST_CERT=0000000000000000000000000000000000000000000000000000000000000000
if bash "$root/scripts/field-device-preflight.sh" "$tmp/test.apk" > "$tmp/fail" 2>&1; then
    echo 'MISMATCHED APK CERT ACCEPTED' >&2; exit 1
fi
echo 'PASS: read-only preflight accepts signed Xiaomi candidate, blocks non-Xiaomi/ambiguous/incorrect certificate, and redacts serial'
