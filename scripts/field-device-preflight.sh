#!/usr/bin/env bash
# Read-only local field preflight. Never installs APKs, changes Android
# settings, pairs Wireless ADB, or prints serials/account identifiers.
set -euo pipefail
umask 077

fail() { printf 'PREFLIGHT_BLOCKED: %s\n' "$*" >&2; exit 1; }

test "$#" -eq 1 || fail "Usage: bash scripts/field-device-preflight.sh /absolute/path/to/candidate.apk"
apk="$1"
test -s "$apk" || fail "Signed candidate does not exist or is empty"
adb_bin="${CNGUARD_ADB_BIN:-adb}"
signer="${CNGUARD_APKSIGNER_BIN:-apksigner}"
cert_file="$(cd "$(dirname "$0")/.." && pwd -P)/release/signing-certificate.pem"
test -s "$cert_file" || fail "Pinned public signing certificate missing"
expected="$(openssl x509 -in "$cert_file" -noout -fingerprint -sha256 |
    sed 's/.*=//' | tr -d ':' | tr '[:upper:]' '[:lower:]')"
[[ "$expected" =~ ^[0-9a-f]{64}$ ]] || fail "Invalid public signing fingerprint"
case "$adb_bin" in */*) test -x "$adb_bin" ;; *) command -v "$adb_bin" >/dev/null ;; esac ||
    fail "Android platform-tools adb not available"
case "$signer" in */*) test -x "$signer" ;; *) command -v "$signer" >/dev/null ;; esac ||
    fail "Android apksigner not available"

actual="$("$signer" verify --min-sdk-version 31 --verbose --print-certs "$apk" |
    awk '/certificate SHA-256 digest:/ {print $NF; exit}' | tr -d ':' | tr '[:upper:]' '[:lower:]')"
test "$actual" = "$expected" || fail "Candidate is not signed by pinned production certificate"
sha="$(sha256sum "$apk" | awk '{print $1}')"
if test -n "${CNGUARD_EXPECTED_APK_SHA256:-}"; then
    test "$sha" = "$CNGUARD_EXPECTED_APK_SHA256" ||
        fail "Signed candidate SHA-256 does not match supplied release manifest"
fi

# Reject ambiguous USB targets rather than select a serial or leak it in logs.
devices="$("$adb_bin" devices -l)"
usb_count="$(printf '%s\n' "$devices" |
    awk '$2=="device" {for(i=3;i<=NF;i++) if($i ~ /^usb:/) {count++;break}} END{print count+0}')"
test "$usb_count" -eq 1 || fail "Exactly one authorized USB Android device required (observed $usb_count)"
prop() { "$adb_bin" -d shell getprop "$1" 2>/dev/null | tr -d '\r' | head -c 128; }
manufacturer="$(prop ro.product.manufacturer)"
brand="$(prop ro.product.brand)"
model="$(prop ro.product.model)"
case "$(printf '%s %s' "$manufacturer" "$brand" | tr '[:upper:]' '[:lower:]')" in
    *xiaomi*|*redmi*|*poco*) ;;
    *) fail "Connected USB device is not a Xiaomi/Redmi/POCO candidate. No operation performed." ;;
esac
release="$(prop ro.build.version.release)"
api="$(prop ro.build.version.sdk)"
rom="$(prop ro.mi.os.version.name)"
region="$(prop ro.miui.region)"
mod_device="$(prop ro.product.mod_device)"
printf 'SIGNED_APK_SHA256=%s\n' "$sha"
printf 'SIGNER_CERT_SHA256=%s\n' "$actual"
printf 'DEVICE_FAMILY_CANDIDATE=%s / %s\n' "$manufacturer" "$brand"
printf 'MODEL=%s\nANDROID=%s\nAPI=%s\n' "$model" "$release" "$api"
printf 'HYPEROS_MARKER=%s\nROM_REGION_MARKER=%s\nROM_DEVICE_VARIANT=%s\n' "$rom" "$region" "$mod_device"
printf '%s\n' 'STOCK_CHINA_ROM_STATUS=UNVERIFIED_BY_PREFLIGHT'
printf '%s\n' 'NEXT=Obtain owner confirmation of stock CN ROM, then follow docs/PHYSICAL_XIAOMI_TEST_PLAN.md'
printf '%s\n' 'NO_INSTALL_NO_WRITES_NO_NETWORK_PAIRING=PASS'
