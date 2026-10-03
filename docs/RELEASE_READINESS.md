# Release Readiness

A successful CI build is necessary but not sufficient for stable-release status.

| Gate | Current state | Evidence required |
| --- | --- | --- |
| Debug unit tests, build and lint | Automated | Green Android CI on the exact release candidate commit |
| Unsigned release build and release lint | Automated | Green Release Readiness workflow |
| Runtime dependency inventory | Automated | Attach the resolved release dependency graph and audit licenses |
| App license and third-party compliance | PARTIAL | GPL-3.0-only owner approval recorded; full resolved dependency/notice and corresponding-source audit still pending |
| Production signing continuity | PARTIAL | Private identity provisioned; public certificate pinned; encrypted backup restore verified; offline recovery copy and production-signed candidate check pending |
| Local Bridge pairing on physical Xiaomi CN ROM | UNVERIFIED | Recorded device/ROM/build pairing and reconnect session |
| Device-local alert presentation | UNVERIFIED | Manual screen-on/off and lock-screen test |
| Greezer and FCM socket read-only probes on target ROM | UNVERIFIED | Physical probe evidence; inaccessible states reported UNKNOWN |
| Remote FCM latency | NOT IMPLEMENTED | Separate consented server-sent probe path and end-to-end evidence |
| State-changing Xiaomi repair | DISABLED | Per-ROM state capture, smallest change, verification and tested rollback |
| Banking-sensitive setup recovery | UNVERIFIED | Confirm ordinary daily operation after Wireless Debugging disabled |

The current CI unsigned release APK is a **build validation artifact**, never a user-facing signed release. CI cannot prove that Android showed a notification popup or that an app's remote FCM push was received.

A diagnostic-only public release may be scoped separately from advanced Xiaomi repair claims after corresponding test and signing gates are met. No stable tag or GitHub Release is authorized merely by green CI.

See [Third-party component review](../THIRD_PARTY_NOTICES.md), [Testing](TESTING.md), and [Support matrix](SUPPORT_MATRIX.md).
