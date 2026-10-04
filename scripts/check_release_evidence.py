#!/usr/bin/env python3
"""Private release gate: no publication, no phone writes, no signing.

Run only after a production-signed APK, physical stock CN-ROM evidence and
distribution/recovery sign-offs exist. Keep evidence OUT of public Git.
"""
import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess
import sys

REQUIRED_TESTS = ("CN-01", "CN-02", "CN-03", "CN-04", "CN-05",
                  "CN-06", "CN-07", "CN-08", "CN-09", "CN-10")
REQUIRED_APPROVALS = ("owner_approved_device_test", "license_and_source_reviewed",
                      "off_host_recovery_verified", "manual_field_qa_signed_off")

def blockers(evidence, apk_hash, expected_source, cert_hash):
    errors = []
    if not isinstance(evidence, dict):
        return ["Evidence must be a JSON object"]
    for label, expected in (("apk_sha256", apk_hash),
                            ("source_commit", expected_source),
                            ("certificate_sha256", cert_hash)):
        if evidence.get(label) != expected:
            errors.append(f"{label} does not match the exact signed candidate")
    for label in ("device_model", "stock_china_rom_build", "android_version"):
        value = evidence.get(label)
        if not isinstance(value, str) or not value.strip() or len(value) > 120:
            errors.append(f"{label} is missing or invalid")
    tests = evidence.get("test_results")
    if not isinstance(tests, dict):
        errors.append("test_results must be an object")
    else:
        for test in REQUIRED_TESTS:
            if tests.get(test) != "PASS":
                errors.append(f"{test} is not a verified physical PASS")
    for label in REQUIRED_APPROVALS:
        if evidence.get(label) is not True:
            errors.append(f"{label} needs explicit owner-reviewed evidence")
    if evidence.get("remote_fcm_stable_claim") is True:
        errors.append("Remote end-to-end FCM has not been implemented/verified")
    if evidence.get("automated_xiaomi_repair_stable_claim") is True:
        errors.append("State-changing Xiaomi repair is not a verified feature")
    return errors

def main():
    parser = argparse.ArgumentParser(description="Local owner-reviewed release readiness check (NEVER publishes)")
    parser.add_argument("--signed-apk", required=True, type=Path)
    parser.add_argument("--evidence", required=True, type=Path, help="PRIVATE redacted field QA JSON, never commit")
    parser.add_argument("--source-commit", required=True)
    parser.add_argument("--apksigner", default="apksigner")
    args = parser.parse_args()
    root = Path(__file__).resolve().parent.parent
    pem = root / "release" / "signing-certificate.pem"
    errors = []
    if not re.fullmatch(r"[0-9a-f]{40}", args.source_commit):
        errors.append("Expected exact 40-character source commit")
    if not args.signed_apk.is_file():
        errors.append("Production-signed APK missing")
    if not pem.is_file():
        errors.append("Pinned public production certificate missing")
    if not args.evidence.is_file():
        errors.append("Private physical test evidence missing")
    if errors:
        for error in errors: print("BLOCKED:", error)
        return 2
    apk_hash = hashlib.sha256(args.signed_apk.read_bytes()).hexdigest()
    try:
        import ssl
        import base64
        import textwrap
        cert_pem = pem.read_text()
        der = ssl.PEM_cert_to_DER_cert(cert_pem)
        cert_hash = hashlib.sha256(der).hexdigest()
        result = subprocess.run(
            [args.apksigner, "verify", "--min-sdk-version", "31",
             "--verbose", "--print-certs", str(args.signed_apk)],
            capture_output=True, text=True, timeout=45, check=True)
        digest = re.search(r"certificate SHA-256 digest:\s*([0-9a-fA-F:]+)", result.stdout)
        if not digest or digest.group(1).replace(":", "").lower() != cert_hash:
            print("BLOCKED: APK signing certificate differs from pinned certificate")
            return 2
    except (OSError, ValueError, subprocess.SubprocessError) as exc:
        print("BLOCKED: APK signature verification unavailable or failed")
        return 2
    try:
        evidence = json.loads(args.evidence.read_text())
    except (OSError, ValueError):
        print("BLOCKED: private QA evidence is not valid JSON")
        return 2
    errors = blockers(evidence, apk_hash, args.source_commit, cert_hash)
    for error in errors:
        print("BLOCKED:", error)
    if errors:
        return 2
    print("EVIDENCE CHECK PASSED: exact APK, cert, source and stated sign-offs match.")
    print("This is not independent physical testing, a legal opinion or permission to publish.")
    print("A human owner must separately approve release scope, artifact and publication.")
    return 0

if __name__ == "__main__":
    sys.exit(main())
