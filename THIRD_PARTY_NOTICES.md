# Third-party component review

This is a preliminary dependency notice and audit ledger for 986 CN Guard. It is **not** a declaration that the full transitive release dependency graph has been cleared for distribution.

## Identified components

| Component | Role | Upstream license | Status |
| --- | --- | --- | --- |
| [Kadb](https://github.com/flyfishxu/Kadb) 2.1.4 | Wireless ADB pairing and protocol | Apache License 2.0 | License text retained in `third_party/licenses/Apache-2.0.txt` |
| [spake2-java](https://github.com/Flyfish233/spake2-java) via Kadb Android pairing | Pairing cryptography | GNU GPL version 3 | Copyleft compliance review required before distribution |
| [Bouncy Castle](https://www.bouncycastle.org/license.html) (BC provider/PKIX pinned 1.86) | Cryptographic primitives | MIT-style Bouncy Castle License | License text retained in `third_party/licenses/Bouncy-Castle.txt` |
| [Kotlin coroutines](https://github.com/Kotlin/kotlinx.coroutines) | Android/coroutine runtime | Apache License 2.0 | Validate resolved graph |
| [Okio](https://github.com/square/okio) via Kadb | I/O | Apache License 2.0 | Validate resolved graph |
| [AndroidX DocumentFile](https://developer.android.com/jetpack/androidx/releases/documentfile) via Kadb | Android storage integration | Apache License 2.0 | Validate resolved graph |
| [LSPosed AndroidHiddenApiBypass](https://github.com/LSPosed/AndroidHiddenApiBypass) via Kadb | Kadb Android runtime | Apache License 2.0 | Review actual usage and compatibility |

Bouncy Castle source states that its license is read in the same way as MIT; its attribution and license text must accompany any binary distribution.

Upstream license files are included for audit and release packaging. They do not assign a license to CN Guard's own source code.

## Release blockers

- Verify the exact resolved runtime dependency graph produced by the Release Readiness workflow, including dependency substitutions and all transitive packages.
- Have the repository owner select and add an appropriate GPL-compatible license for any distributed combined APK that includes GPL-licensed SPAKE2 code. Do not represent an undetermined application license as resolved.
- Preserve and distribute all applicable notices and full license texts with the release package/source offer, and review other transitive components that are not listed here.
- Confirm how the chosen application license and distribution channel will meet the corresponding-source obligations before publishing a stable APK.
- Verify a persistent production signing certificate, physical device behavior and user-facing documentation independently of this dependency audit.

This document records project engineering checks and does not substitute for legal review.
