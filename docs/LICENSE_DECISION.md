# Owner-approved application license

**Status: APPROVED — GPL-3.0-only.** On 4 October 2026 (Asia/Kuala_Lumpur), the repository owner explicitly approved GNU GPL version 3 for CN Guard and approved provisioning a dedicated production signing identity outside GitHub.

The [repository-root LICENSE](../LICENSE) supplies the complete GPLv3 license text; this project selects **GPL-3.0-only** rather than GPL-3.0-or-later. Original project contributions are distributed under that license to the extent the owner and each contributor have the necessary rights. This does not override separate upstream copyright notices or relicense third-party components.

The existing Kadb Android pairing integration resolves `spake2-java` under GPL-3.0, and is retained rather than silently replaced. See [third-party review](../THIRD_PARTY_NOTICES.md) for the resolved dependency graph and identified component licenses.

## Remaining compliance gates

- Confirm ownership or contributor permission for all original work and whether any third-party file requires separate treatment.
- Complete the *final* release runtime transitive license/attribution audit, preserve every applicable notice and supply complete corresponding source/build instructions with a distributed combined APK.
- Keep private keystore/passwords/recovery material exclusively on trusted private storage; the repository includes only a public certificate.
- Finish physical China-ROM verification and source-backed support claims before stable release.

This approval resolves **which license the owner selected**. It does **not** by itself satisfy every redistribution or physical testing obligation.
