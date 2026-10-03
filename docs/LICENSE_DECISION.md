# Application-license decision (owner approval required)

**Status:** NEED DECISION. This document is not a license grant, and CN Guard does not currently have an owner-approved project-wide LICENSE file.

The current application bundles Kadb's Android pairing path, which resolves `spake2-java:1.1.1` under GPL-3.0. The Free Software Foundation's [GPL FAQ](https://www.gnu.org/licenses/gpl-faq.en.html#IfLibraryIsGPL) explains that distributing a combined application which links to a GPL library imposes GPL terms on the combination. The copyright holder's approval is necessary before assigning a project license or distributing a combined APK.

## Option A — retain the tested Kadb pairing path and approve GPLv3

- Retain build-verified pairing integration and current feature set.
- Project copyright holder explicitly approves using GPLv3 for the project and identifies all actual contributors/rightsholders before a project LICENSE is adopted.
- Finish full resolved-transitive licensing/attribution audit; ensure that releases provide appropriate corresponding source, build instructions and notices.
- Keep actual China-ROM physical validation and production signing separate from the licensing decision.

## Option B — require a permissive application license

- Do **not** relicense GPLv3 SPAKE2 simply by changing a wrapper or omitting the notice.
- Treat replacement of the GPL-dependent pairing implementation as a new security-sensitive engineering project. Select a truly compatible independently licensed pairing implementation, inspect every transitive dependency, and validate its protocol/security behavior with independent tests and physical devices before replacing Kadb.
- Until a replacement passes, the pairing-enabled APK remains blocked from permissive distribution. A separate diagnostic-only variant may be designed without bundling the GPL-dependent code, but it must not falsely claim Local Bridge pairing is available.

**No selection is recorded in this document.** Release publication is blocked pending the owner's explicit choice and a final legal/compliance review.
