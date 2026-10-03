# Resolved runtime dependency license inventory

Snapshot: commit `05a74b4b9ef9c499553f6b898fd3e82c3d9237ba`, Release Readiness run [37138881500](https://github.com/AbeyyN/986-CNGuard/actions/runs/37138881500). Every new candidate must regenerate the graph; versions listed here are not forecasts.

| Resolved runtime modules | License category evidenced by upstream or bundled license | Obligations / release handling |
| --- | --- | --- |
| `com.flyfishxu:kadb-android:2.1.4` | Apache-2.0 | Bundle upstream attribution and license |
| `com.github.Flyfish233:spake2-java:1.1.1` | GPL-3.0 | Combined CN Guard source approved GPL-3.0-only; complete corresponding source/distribution review |
| `cafe.cryptography:ed25519-elisabeth:0.1.0` | MIT | Preserve upstream attribution and license |
| `cafe.cryptography:curve25519-elisabeth:0.1.0` | MIT and upstream derived-code notice | Preserve full upstream notice as committed |
| `org.bouncycastle:bcprov/bcpkix/bcutil-jdk18on:1.86` | Bouncy Castle MIT-style | Bundle exact full license and attribution |
| `org.jetbrains.kotlin:kotlin-stdlib:2.4.20`; `kotlinx-coroutines-core/core-jvm/android:1.11.0` | Apache-2.0 | Root Apache text retained; verify any packaged special notices |
| `com.squareup.okio:okio/okio-jvm:3.17.0` | Apache-2.0 | Root Apache text retained; verify any packaged special notices |
| `androidx.documentfile:documentfile:1.1.0`, `androidx.annotation:annotation/annotation-jvm:1.8.1`, `annotation-experimental:1.1.0` | AndroidX Apache-2.0 | Root Apache text retained |
| `androidx.core:core:1.7.0`; `androidx.lifecycle:lifecycle-runtime/lifecycle-common:2.3.1` | AndroidX Apache-2.0 | Root Apache text retained |
| `androidx.arch.core:core-runtime/core-common:2.1.0`; `androidx.versionedparcelable:versionedparcelable:1.1.1` | AndroidX Apache-2.0 | Root Apache text retained |
| `androidx.collection:collection:1.0.0`; `androidx.concurrent:concurrent-futures:1.0.0` | AndroidX Apache-2.0 | Root Apache text retained |
| `com.google.guava:listenablefuture:1.0` | Apache-2.0 (Guava distribution) | Verify artifact-specific embedded license/notice before publication |
| `org.jspecify:jspecify:1.0.0` | Apache-2.0 | Root Apache text retained |
| `org.jetbrains:annotations:23.0.0` | Apache-2.0 | Root Apache text retained |
| `org.lsposed.hiddenapibypass:hiddenapibypass:6.1` | Apache-2.0 | Root Apache text retained; review Android hidden-API compatibility before claiming it works |

This table describes identified resolved dependencies only; it does not certify legal compliance, contributor provenance, or the absence of licenses in embedded resources. `third_party/licenses/` preserves GPL-3.0, Apache-2.0, Bouncy Castle and each Elisabeth license/notice. Package those alongside any production APK and provide the corresponding GPL source.

**Distribution gate:** owner review of final generated dependency graph, packaged notices/attributions, source availability, OEM-specific safety claims, physical CN-ROM test evidence and signing certificate continuity. CI generates an unsigned candidate to support off-host private signing; GitHub Actions archives are not the permanent artifact store.
