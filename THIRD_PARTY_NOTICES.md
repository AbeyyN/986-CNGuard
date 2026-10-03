# Third-party component review

This is a preliminary dependency notice and audit ledger for 986 CN Guard. It is **not** a declaration that the full transitive release dependency graph has been cleared for distribution.

## Identified components

| Component | Role | Upstream license | Status |
| --- | --- | --- | --- |
| [Kadb](https://github.com/flyfishxu/Kadb) 2.1.4 | Wireless ADB pairing and protocol | Apache License 2.0 | License text retained in `third_party/licenses/Apache-2.0.txt` |
| [spake2-java](https://github.com/Flyfish233/spake2-java) via Kadb Android pairing | Pairing cryptography | GNU GPL version 3 | Copyleft compliance review required before distribution |
| [ed25519-elisabeth](https://github.com/cryptography-cafe/ed25519-elisabeth) 0.1.0 | SPAKE2 transitive signature library | MIT | Upstream copyright/license retained in `third_party/licenses/Ed25519-Elisabeth-MIT.txt` |
| [curve25519-elisabeth](https://github.com/cryptography-cafe/curve25519-elisabeth) 0.1.0 | SPAKE2 transitive elliptic-curve library | MIT, with upstream third-party provenance in its LICENSE | Upstream license and derived-code notices retained in `third_party/licenses/Curve25519-Elisabeth-License.txt` |
| [Bouncy Castle](https://www.bouncycastle.org/license.html) (BC provider/PKIX pinned 1.86) | Cryptographic primitives | MIT-style Bouncy Castle License | License text retained in `third_party/licenses/Bouncy-Castle.txt` |
| [Kotlin coroutines](https://github.com/Kotlin/kotlinx.coroutines) | Android/coroutine runtime | Apache License 2.0 | Validate resolved graph |
| [Okio](https://github.com/square/okio) via Kadb | I/O | Apache License 2.0 | Validate resolved graph |
| [AndroidX DocumentFile](https://developer.android.com/jetpack/androidx/releases/documentfile) via Kadb | Android storage integration | Apache License 2.0 | Validate resolved graph |
| [LSPosed AndroidHiddenApiBypass](https://github.com/LSPosed/AndroidHiddenApiBypass) via Kadb | Kadb Android runtime | Apache License 2.0 | Review actual usage and compatibility |

Bouncy Castle source states that its license is read in the same way as MIT; its attribution and license text must accompany any binary distribution.

Upstream license files are included for audit and release packaging. They do not assign a license to CN Guard's own source code.

## Resolved release dependency snapshot

The Release Readiness workflow run [37120817156](https://github.com/AbeyyN/986-CNGuard/actions/runs/37120817156) produced the first checked resolved `releaseRuntimeClasspath` inventory. Its graph includes:

- Kadb Android **2.1.4** -> SPAKE2 Java **1.1.1** -> ed25519-elisabeth **0.1.0** and curve25519-elisabeth **0.1.0**.
- Bouncy Castle `bcprov`, `bcpkix`, and `bcutil` all resolved to **1.86**. The Kadb transitive request for 1.84 was overridden by this project.
- Okio runtime **3.17.0**, AndroidX DocumentFile **1.1.0**, AndroidHiddenApiBypass **6.1**, Kotlin standard library **2.4.20**, Kotlin coroutines **1.11.0**.

These are the versions in that single audited CI run; each release candidate must regenerate and review its own inventory. Other AndroidX, annotations and transitive artifact notices still need a distribution audit.

## Release blockers

- Re-run and audit the exact resolved runtime dependency graph for the final release candidate, including third-party notice obligations for all remaining transitives.
- Have the repository owner select and add an appropriate GPL-compatible license for any distributed combined APK that includes GPL-licensed SPAKE2 code. Do not represent an undetermined application license as resolved.
- Preserve and distribute all applicable notices and full license texts with the release package/source offer, and review other transitive components that are not listed here.
- Confirm how the chosen application license and distribution channel will meet the corresponding-source obligations before publishing a stable APK.
- Verify a persistent production signing certificate, physical device behavior and user-facing documentation independently of this dependency audit.

This document records project engineering checks and does not substitute for legal review.
