# Owner-operated signing and off-host recovery

**Current engineering state:** A dedicated production signing identity has been provisioned privately and its encrypted on-server backup was previously restore-tested. The latest source build may still be **unsigned** until its exact production-signed artifact is independently verified. CI uses an unrelated disposable TEST-ONLY signing key.

**Important boundary:** Release signing is an owner-operated action on a trusted host. Never use GitHub Actions, public scripts, a remote assistant, chat logs or cloud storage to transfer the production keystore or either password. Avoid shell tracing (\`set -x\`) and pasting credentials into terminals, logs or support reports.

## Owner's private signing session

1. Independently inspect the exact Git commit, CI run, unsigned APK's SHA-256 and pinned public certificate shown in \`release/signing-certificate.pem\`. Ensure the source directory has a matching \`git rev-parse HEAD\` and the output is inside \`experimental/\`, NOT the authoritative \`latest/\` area.
2. Log in to the trusted signing host through your own trusted terminal and open a private root shell. Do not delegate secret reading to a remote tool. Ensure the actual production keystore and existing root-only password files are accessible locally. Do not create a new production identity for an update.
3. Supply the existing local keystore path, alias, review-pinned public certificate digest, exact unsigned APK path, a **new nonexisting** output path and Android Build Tools path through protected environment variables as described in [offline signing](OFFLINE_SIGNING.md). Read the existing passwords in that local interactive trusted shell without printing or logging them, then run \`scripts/sign-release-local.sh\`.
4. Confirm \`apksigner verify --verbose --print-certs\`, compare the cert SHA-256 byte-for-byte with the published public certificate, \`zipalign -c 4\` and independently compute the signed APK SHA-256. Clear both credential variables from the shell. Never promote the result automatically.
5. Create a private manifest naming **the exact source commit actually built**, unsigned checksum, signed checksum and verified certificate fingerprint. A later documentation-only commit is not the source commit for an earlier APK.

## Complete disaster recovery without disclosing secrets

**The encrypted backup on a different disk within the same host is not an off-host backup.** The private root-only recovery passphrase currently requires a separate, owner-controlled offline copy.

- Physically attach *your own* encrypted removable medium to the trusted host, or transfer the encrypted archive to another owner-controlled encrypted offline host through an approved, end-to-end verified channel. Never repartition, format or erase existing media as part of this process.
- Transfer the AES-256 encrypted backup file and its checksum to the independent storage. Keep its **passphrase on a separately secured offline medium or offline password vault** — not next to the encrypted backup and not in public cloud drives or GitHub.
- From a trusted offline owner-operated machine, demonstrate decryption into a restricted temporary directory and match SHA-256 hashes of the restored keystore and signing-password file to the originals without writing those contents into any console output. Clear temporary restored secrets afterward according to the owner's storage policy.
- Record only the off-host storage reference, archive checksum, verification date and \`PASS/FAIL\` in the private canonical handoff. Do not include removable drive serial numbers, password text or private key bytes. If the media is unavailable, report \`PENDING\`; do not infer completion.

## Stable release gate

A signed APK alone does not establish a stable release. Physically validate an exact candidate on verified stock Xiaomi China-ROM hardware under the owner's explicit device-testing consent. The private field evidence must record CN-01 through CN-10 observations with exact APK SHA-256; it must not contain sensitive identifiers, notification text or pairing codes. Remote end-to-end FCM and state-changing repairs are **not yet implemented** and must not be advertised as stable features. Complete source/attribution review, off-host custody and owner publication approval before a public release.

The repository provides \`scripts/check_release_evidence.py\` for a **local read-only consistency check** of owner-attested evidence, source identity and APK signature. Passing synthetic CI tests proves only the checker logic; it does not substitute for actual field testing, an independent legal review or owner authorization to publish.
