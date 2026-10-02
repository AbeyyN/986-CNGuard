# Privacy

986 CN Guard is designed to diagnose device compatibility problems locally.

## Default behavior

The current application does not operate a telemetry or analytics service. Diagnostic results remain on the device unless a future feature explicitly asks the user to share a report.

## Sensitive information

The project does not require notification message contents for its diagnostic model. The following data is outside the scope of diagnostic collection and must not be uploaded by CN Guard:

- notification titles or message bodies;
- SMS, OTP or authentication codes;
- banking or payment data;
- usernames, passwords or session tokens;
- Google or Xiaomi account identifiers;
- phone numbers;
- IMEI, IMSI or serial numbers;
- Android ID or advertising identifiers;
- contact, call or message databases.

## Local Bridge

Wireless ADB pairing codes are used only for the active pairing attempt and are not persisted.

The ADB host private key used by Local Bridge is stored in the application's private data directory. It is not part of diagnostic reports and is not uploaded by the application.

## Notification diagnostics

If optional notification access is introduced, the diagnostic design is limited to delivery metadata required to determine whether a notification was posted and how it was ranked or presented. Notification content is not required and must not be retained by the diagnostic history.

## Future diagnostic sharing

Any future report-upload feature must be:

- opt-in;
- reviewable before submission;
- minimized to technical compatibility data;
- documented with its destination and retention policy;
- disabled by default.

A report must remain useful without stable device identifiers.

## Uninstalling

Uninstalling the application removes its application-private data, including the Local Bridge host identity stored by the app. Android may separately retain Wireless Debugging trusted-device records until the user removes them in system settings.
