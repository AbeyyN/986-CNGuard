# Corresponding-source publication requirements

CN Guard is GPL-3.0-only and presently uses a GPL-3.0 SPAKE2 component through its Kadb pairing implementation. A publicly distributed combined APK must have appropriately accessible corresponding source and preserve all applicable third-party license/copyright notices.

For each release, record the exact application source commit, resolved dependency graph, build tools and the final signed APK checksum. Package exact project source, applicable third-party component sources, all required notices/licenses and the scripts needed to rebuild the published version. The current build baseline is JDK 17, Gradle 9.6.0, Android SDK Platform 37.2 and Build Tools 37.0.0. The normal release-validation commands are Gradle tasks app:testDebugUnitTest, app:assembleRelease and app:lintRelease.

When the public downloads are actually published, provide comparably accessible corresponding source at the same location or clearly link to an equivalent persistent download location. Linking only to the current development branch is insufficient if the APK was built from a different commit. Retain source access for the required period. Keep the production signing private key and recovery passphrase out of public source, CI and release downloads.

Private collection status for app source commit a03350ba2a05baddc79f9293bd3b6b93760ea7ed: 30 runtime coordinates, 3 metadata/BOM wrappers, 27 of 27 remaining component source archives downloaded and integrity tested. Exact upstream Kadb 2.1.4 and SPAKE2 Java 1.1.1 source snapshots, license files and a Guava inherited-license audit are included in the private collection.

**Collection is not owner approval for public redistribution, nor proof of Xiaomi China-ROM compatibility.** Final notice/source review, physically verified device tests, owner-controlled off-host recovery and an independently verified production-signed candidate remain required.

See the [dependency audit](RELEASE_DEPENDENCY_AUDIT.md), [release readiness](RELEASE_READINESS.md) and [owner signing/recovery instructions](OWNER_SIGNING_AND_OFFHOST_RECOVERY.md).
