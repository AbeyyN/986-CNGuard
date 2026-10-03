# Public production signing certificate

The repository owner approved creation of a new, dedicated production Android signing identity on **4 October 2026** (Asia/Kuala_Lumpur). The full public X.509 certificate is provided in [signing-certificate.pem](signing-certificate.pem). This directory must never contain the private keystore, passwords or backup recovery secret.

- Certificate SHA-256 (normalized): `A693C068E91E410424DE88FD5BA42C60A04E13A4EDD4D271E17E694B1E195CDE`
- Alias: `cnguard-release`
- Subject: `CN=986 CN Guard, OU=Android Release, O=AbeyyTechXy, C=MY`
- Validity: 3 October 2026 UTC to 25 September 2056 UTC

For future production packages, compare the APK signer certificate to this independently retained public fingerprint; a valid signature with a *different* certificate is not an authorized upgrade. The presence of this certificate does not mean a production APK has been signed or published.

The private key and its encrypted backup are kept outside the public repository under the project's private artifact governance. Offline custody of the recovery secret and signed candidate verification remain separate gates.
