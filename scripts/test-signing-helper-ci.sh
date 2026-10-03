#!/usr/bin/env bash
# Disposable CI verification. Never use this identity for distribution.
set -euo pipefail
umask 077

: "${RUNNER_TEMP:?GitHub runner temp directory required}"
: "${ANDROID_HOME:?Android SDK required}"
tools="$ANDROID_HOME/build-tools/37.0.0"
test -x "$tools/apksigner"
test -x "$tools/zipalign"

unsigned="$(find app/build/outputs/apk/release -maxdepth 1 -name '*.apk' -type f -print -quit)"
test -n "$unsigned" && test -s "$unsigned"

tmp="$(mktemp -d "$RUNNER_TEMP/cnguard-disposable-sign-XXXXXXXX")"
trap 'rm -rf -- "$tmp"' EXIT

# Disposable random credential/identity belongs exclusively to this test job.
# Keep stdout clean: never echo test credentials or private key materials.
pass="$(openssl rand -hex 24)"
keytool -genkeypair -noprompt -storetype PKCS12 \
  -keystore "$tmp/disposable-only.p12" \
  -alias disposable-ci \
  -storepass "$pass" -keypass "$pass" \
  -keyalg RSA -keysize 3072 -validity 1 \
  -dname "CN=CN Guard Disposable CI Test, O=Test Only" \
  > "$tmp/keytool.log" 2>&1

fingerprint="$(keytool -list -v \
  -keystore "$tmp/disposable-only.p12" -storepass "$pass" \
  -alias disposable-ci |
  sed -n 's/^[[:space:]]*SHA256: //p' | head -n 1)"
test -n "$fingerprint"

export CNGUARD_UNSIGNED_APK="$unsigned"
export CNGUARD_RELEASE_KEYSTORE="$tmp/disposable-only.p12"
export CNGUARD_RELEASE_ALIAS="disposable-ci"
export CNGUARD_KEYSTORE_PASSWORD="$pass"
export CNGUARD_KEY_PASSWORD="$pass"
export CNGUARD_EXPECTED_CERT_SHA256="$fingerprint"
export CNGUARD_ANDROID_BUILD_TOOLS="$tools"
export CNGUARD_SIGNED_APK="$tmp/signed-only-for-ci.apk"

bash scripts/sign-release-local.sh > "$tmp/signing.log"
test -s "$CNGUARD_SIGNED_APK"
"$tools/apksigner" verify --verbose --print-certs "$CNGUARD_SIGNED_APK"   > "$tmp/verification.log"

# A wrong pinned cert MUST fail closed and MUST NOT publish any output.
if CNGUARD_EXPECTED_CERT_SHA256=0000000000000000000000000000000000000000000000000000000000000000     CNGUARD_SIGNED_APK="$tmp/wrong-cert-must-not-exist.apk"     bash scripts/sign-release-local.sh > "$tmp/mismatch.log" 2>&1; then
  echo "FAIL: fingerprint mismatch was accepted" >&2
  exit 1
fi
test ! -e "$tmp/wrong-cert-must-not-exist.apk"

# An existing artifact MUST never be silently overwritten.
before="$(sha256sum "$CNGUARD_SIGNED_APK" | awk '{print $1}')"
if bash scripts/sign-release-local.sh > "$tmp/overwrite.log" 2>&1; then
  echo "FAIL: existing signed output was overwritten" >&2
  exit 1
fi
after="$(sha256sum "$CNGUARD_SIGNED_APK" | awk '{print $1}')"
test "$before" = "$after"

echo "PASS: disposable signing, Android verification, mismatched cert rejection, overwrite protection"
echo "Disposable test-signed APK and private key were NOT uploaded and will be deleted on exit."
