# Offline release signing

This is a guarded helper for 986 or another trusted offline build host. It is **not** a key-generation system and does not upload or publish artifacts to GitHub. The project has a private, previously provisioned production signing identity and a tested encrypted on-server backup. Off-host recovery-secret custody and exact latest-candidate signing must still be verified. Do not call a debug APK or an unsigned candidate stable. See [owner signing and offline recovery](OWNER_SIGNING_AND_OFFHOST_RECOVERY.md).

Before use:

1. Finalize the application license and third-party distribution obligations.
2. Build and validate the release candidate from an exact reviewed Git commit. The CI release-readiness job verifies an **unsigned** release build and supplies its checksum/dependency graph; it does not distribute a signed APK.
3. Use the owner-approved existing production keystore **outside the public repository**. Never silently regenerate or replace it. The key and passwords remain under private owner control.
4. Record and independently review the public SHA-256 signing certificate fingerprint. Pin that value in the protected signing environment so future releases cannot accidentally use a different certificate.
5. Set the following variables through a protected environment, without putting passwords in the command line or GitHub Actions:

   - `CNGUARD_UNSIGNED_APK`: existing validated unsigned APK.
   - `CNGUARD_RELEASE_KEYSTORE`: existing keystore outside the source tree.
   - `CNGUARD_RELEASE_ALIAS`: existing keystore alias.
   - `CNGUARD_KEYSTORE_PASSWORD` and `CNGUARD_KEY_PASSWORD`: private credentials.
   - `CNGUARD_EXPECTED_CERT_SHA256`: independently reviewed public certificate fingerprint.
   - `CNGUARD_SIGNED_APK`: new absolute output path in the authorized artifact store, outside GitHub.
   - `CNGUARD_ANDROID_BUILD_TOOLS`: Android build-tools directory containing `zipalign` and `apksigner`.

Run `bash scripts/sign-release-local.sh` from the reviewed source checkout. The helper validates required inputs, refuses in-repository secrets/output and existing target files, aligns the APK, signs it with the Android v2/v3 scheme supported by the target SDK, explicitly disables the optional v4 sidecar to avoid orphaned staging files, verifies the signature, checks certificate continuity and prints the signed-file checksum.

**No stable release** is established by a successful signing command alone. The support matrix and physical Xiaomi CN-ROM, banking-sensitive setup and notification/FCM verification gates must also be satisfied. Keep the signed APK under the project's authoritative artifact policy rather than making GitHub source or CI logs a signing-secret store.

See [Android apksigner documentation](https://developer.android.com/tools/apksigner).
