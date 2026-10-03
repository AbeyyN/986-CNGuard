#!/usr/bin/env bash
# Offline-only signing helper. Requires an owner-approved, pre-existing keystore.
set -euo pipefail
umask 077

fail() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }

: "${CNGUARD_UNSIGNED_APK:?Set CNGUARD_UNSIGNED_APK to an existing unsigned release APK}"
: "${CNGUARD_RELEASE_KEYSTORE:?Set CNGUARD_RELEASE_KEYSTORE to a pre-existing private keystore}"
: "${CNGUARD_RELEASE_ALIAS:?Set CNGUARD_RELEASE_ALIAS}"
: "${CNGUARD_KEYSTORE_PASSWORD:?Supply password from a protected environment, never the CLI}"
: "${CNGUARD_KEY_PASSWORD:?Supply key password from a protected environment}"
: "${CNGUARD_EXPECTED_CERT_SHA256:?Pin the reviewed public certificate SHA-256 fingerprint}"
: "${CNGUARD_SIGNED_APK:?Set an absolute destination outside the public repository}"
: "${CNGUARD_ANDROID_BUILD_TOOLS:?Set the local Android build-tools directory}"

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd -P)"
unsigned="$(realpath -e "$CNGUARD_UNSIGNED_APK")"
keystore="$(realpath -e "$CNGUARD_RELEASE_KEYSTORE")"
output="$CNGUARD_SIGNED_APK"
case "$output" in /*) ;; *) fail "Destination must be an absolute path" ;; esac
output="$(realpath -m "$output")"
outdir="$(dirname "$output")"
test -d "$outdir" || fail "Destination directory must exist"
test -s "$unsigned" || fail "Unsigned APK missing or empty"
test -s "$keystore" || fail "Keystore missing or empty"
test ! -e "$output" || fail "Refusing to overwrite an existing signed APK"

case "$keystore" in "$root"/*) fail "Keystore cannot be inside the public repository" ;; esac
case "$output" in "$root"/*) fail "Signed output cannot be inside the public repository" ;; esac

zipalign="$CNGUARD_ANDROID_BUILD_TOOLS/zipalign"
apksigner="$CNGUARD_ANDROID_BUILD_TOOLS/apksigner"
test -x "$zipalign" || fail "zipalign not executable"
test -x "$apksigner" || fail "apksigner not executable"

aligned="$(mktemp "$outdir/.cnguard-aligned.XXXXXXXX.apk")"
staged="$(mktemp "$outdir/.cnguard-signed.XXXXXXXX.apk")"
trap 'rm -f -- "$aligned" "$staged"' EXIT
rm -f -- "$aligned" "$staged"

"$zipalign" -p 4 "$unsigned" "$aligned"

"$apksigner" sign \
  --ks "$keystore" \
  --ks-key-alias "$CNGUARD_RELEASE_ALIAS" \
  --ks-pass env:CNGUARD_KEYSTORE_PASSWORD \
  --key-pass env:CNGUARD_KEY_PASSWORD \
  --v4-signing-enabled false \
  --out "$staged" \
  "$aligned"

report="$("$apksigner" verify --verbose --print-certs "$staged")"
# Android Build Tools 37 uses "V2 Signer:" or "V3.0 Signer:" rather than
# the older "Signer #1" label. Match certificate digest, not public key.
fingerprint="$(printf '%s' "$report" |
  awk '/certificate SHA-256 digest:/ {print $NF; exit}')"
test -n "$fingerprint" || fail "Cannot extract signing certificate fingerprint"

actual="$(printf '%s' "$fingerprint" | tr -d ':' | tr '[:upper:]' '[:lower:]')"
expected="$(printf '%s' "$CNGUARD_EXPECTED_CERT_SHA256" | tr -d ':' | tr '[:upper:]' '[:lower:]')"
[[ "$actual" =~ ^[0-9a-f]{64}$ ]] || fail "Invalid actual certificate digest"
[[ "$expected" =~ ^[0-9a-f]{64}$ ]] || fail "Expected certificate digest must be 64 hex characters"
test "$actual" = "$expected" || fail "Certificate fingerprint mismatch; no signed output published"
ln "$staged" "$output" || fail "Could not atomically publish signed output"
printf 'SIGNATURE VERIFIED; certificate SHA-256: %s\n' "$actual"
sha256sum "$output"
printf 'Output: %s\n' "$output"
