import unittest
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from scripts.check_release_evidence import blockers, REQUIRED_TESTS, REQUIRED_APPROVALS

APK = "a" * 64
SRC = "b" * 40
CERT = "c" * 64

def fixture():
    evidence = {
        "apk_sha256": APK, "source_commit": SRC, "certificate_sha256": CERT,
        "device_model": "Synthetic Xiaomi", "stock_china_rom_build": "SYNTHETIC_ONLY",
        "android_version": "16",
        "test_results": {key: "PASS" for key in REQUIRED_TESTS},
        "remote_fcm_stable_claim": False,
        "automated_xiaomi_repair_stable_claim": False,
    }
    for key in REQUIRED_APPROVALS:
        evidence[key] = True
    return evidence

class ReleaseEvidenceLogicTest(unittest.TestCase):
    def test_synthetic_complete_fixture_exercises_logic_only(self):
        self.assertEqual([], blockers(fixture(), APK, SRC, CERT))

    def test_no_fake_physical_qa_pass(self):
        value = fixture()
        value["test_results"]["CN-05"] = "NOT_TESTED"
        self.assertIn("CN-05 is not a verified physical PASS", blockers(value, APK, SRC, CERT))

    def test_no_cross_version_binary(self):
        self.assertTrue(any("apk_sha256" in x for x in blockers(fixture(), "d" * 64, SRC, CERT)))

    def test_no_auto_release_without_off_host_custody(self):
        value = fixture()
        value["off_host_recovery_verified"] = False
        self.assertTrue(any("off_host_recovery" in x for x in blockers(value, APK, SRC, CERT)))

    def test_never_certify_unimplemented_features(self):
        value = fixture()
        value["automated_xiaomi_repair_stable_claim"] = True
        self.assertTrue(any("State-changing" in x for x in blockers(value, APK, SRC, CERT)))

    def test_empty_or_missing_evidence_fails(self):
        self.assertGreater(len(blockers({}, APK, SRC, CERT)), 10)

if __name__ == "__main__":
    unittest.main()
