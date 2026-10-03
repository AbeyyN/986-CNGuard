# Device and ROM classification

The baseline distinguishes **Xiaomi-family signals** from **verified China ROM** support.

A manufacturer or brand value, the presence of Security Center or PowerKeeper, and the phone's language are not proof that a device is running a China ROM. The default ROM-region result is therefore UNKNOWN.

The classifier is intentionally conservative:

- Xiaomi manufacturer can establish a Xiaomi-family signal.
- Redmi/POCO branding needs corroborating Xiaomi vendor-package presence when manufacturer is not Xiaomi.
- Other manufacturers can still use general Android notification diagnostics.
- Future vendor adapters must identify and validate their supported mechanism at runtime.

Physical-device/build verification is required before a China-ROM support claim is added to the support matrix.
