# Guided Safe Fixes

Guided Safe Fixes use supported Android Settings intents with Xiaomi-specific navigation attempts and Android fallbacks.

Current supported pages:

- app notification settings with per-app Details fallback;
- per-app system Details;
- Xiaomi Autostart management when exposed by the ROM;
- general Android battery optimization controls.

## Safety contract

Opening a Settings page is **not verification of a fix**. CN Guard reports only that guidance was opened. The user must choose the setting change and return for a fresh read-only diagnostic probe where one is available.

The app does not automate taps with Accessibility, manipulate banking app settings, or treat a permission grant as evidence that late notifications have been repaired.

Package-targeted actions validate package-name syntax before an intent is constructed. Xiaomi pages may be missing or renamed; all vendor-specific navigation paths have a framework-level fallback.

## Verification limits

Android's normal third-party APIs do not consistently expose another app's Xiaomi-specific battery or Autostart state. When verification is unavailable, the result remains **unverified**. The UI must not display a successful repair state based only on returning from a Settings page.
