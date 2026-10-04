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
| [Guava ListenableFuture](https://github.com/google/guava) 1.0 via AndroidX | Small interface-only dependency | Apache License 2.0, inherited from guava-parent 26.0-android and explicitly stated in source copyright header | Preserve Apache attribution and exact source; reviewed in source audit |

Bouncy Castle source states that its license is read in the same way as MIT; its attribution and license text must accompany any binary distribution.

The original CN Guard project source is now owner-approved under GPL-3.0-only; see the repository-root `LICENSE`. These bundled upstream licenses and notices remain the respective rights-holders' terms, and inclusion in this project does not relicense their separate source.

## Resolved release dependency snapshot

The Release Readiness workflow run [37120817156](https://github.com/AbeyyN/986-CNGuard/actions/runs/37120817156) produced the first checked resolved `releaseRuntimeClasspath` inventory. Its graph includes:

- Kadb Android **2.1.4** -> SPAKE2 Java **1.1.1** -> ed25519-elisabeth **0.1.0** and curve25519-elisabeth **0.1.0**.
- Bouncy Castle `bcprov`, `bcpkix`, and `bcutil` all resolved to **1.86**. The Kadb transitive request for 1.84 was overridden by this project.
- Okio runtime **3.17.0**, AndroidX DocumentFile **1.1.0**, AndroidHiddenApiBypass **6.1**, Kotlin standard library **2.4.20**, Kotlin coroutines **1.11.0**.

These are the versions in that single audited CI run; each release candidate must regenerate and review its own inventory. The full resolved AndroidX/annotation/Google/JetBrains subset is itemized in [Release dependency license inventory](docs/RELEASE_DEPENDENCY_AUDIT.md). Exact embedded notices and full final publication compliance remain a separate human gate.

## Release blockers

- Re-run and audit the exact resolved runtime dependency graph for the final release candidate, including third-party notice obligations for all remaining transitives.
- **Owner GPL-3.0-only license decision: APPROVED and applied.** Verify the owner's authority over all original contributions and preserve GPL source-distribution obligations before each binary distribution.
- Preserve and distribute all applicable notices and full license texts with the release package/source offer, and review other transitive components that are not listed here.
- Supply complete corresponding source, reproducible build instructions and every required third-party notice with any published combined APK. Confirm delivery obligations and review any compatibility uncertainties prior to distribution.
- Verify a persistent production signing certificate, physical device behavior and user-facing documentation independently of this dependency audit.

This document records project engineering checks and does not substitute for legal review.
