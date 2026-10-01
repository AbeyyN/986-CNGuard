# Architecture

986 CN Guard is designed as a capability-driven Android diagnostics platform rather than a collection of HyperOS-version-specific patches.

## Layers

### Core diagnostics

The core gathers read-only facts that can be obtained safely from the Android framework and package manager.

Examples:

- device and Android build information;
- Google Play services presence;
- Xiaomi system-package presence;
- readable Xiaomi settings;
- notification permission and channel state;
- per-app execution signals exposed through supported APIs.

The core does not require root or Shizuku.

### Xiaomi adapters

Xiaomi-specific behavior is isolated behind adapters.

An adapter is responsible for:

1. detecting whether a mechanism exists;
2. reporting its capability state;
3. exposing only operations supported by that mechanism;
4. verifying the result after a change;
5. providing a rollback path where the state is mutable.

Adapters must tolerate missing services, renamed internals, changed AppOps, and permission denial without crashing the app.

### Privilege bridges

Higher-privilege operations are optional and ordered by user friction:

1. Local Bridge
2. Shizuku
3. root

Local Bridge is the primary advanced path. It uses a built-in Wireless ADB client so users do not need Termux or Shizuku for supported shell-level actions. Shizuku and root remain fallback adapters for capabilities that cannot be delivered safely through Local Bridge.

The application must remain useful without any privilege bridge. Privilege acquisition is never part of ordinary startup.

See [LOCAL_BRIDGE.md](LOCAL_BRIDGE.md).

### Diagnostic model

A diagnostic result contains:

- identifier;
- state;
- short summary;
- optional technical detail;
- evidence source;
- whether a safe action is available.

The UI consumes this model rather than calling Xiaomi internals directly.

### Repair actions

Repair actions are separate from probes.

Each action follows:

```text
detect -> preflight -> snapshot -> apply -> verify
                                  |
                                  +-> rollback
```

This separation prevents a read-only scan from modifying the device.

Privileged repair actions are typed and allowlisted. The application does not expose an arbitrary command terminal, and remote rule packs cannot inject shell text.

## Compatibility strategy

Version numbers are secondary signals.

The preferred decision order is:

1. detect device/vendor;
2. probe the required service, setting, AppOp, package, or API;
3. select the matching adapter;
4. fall back to unknown/unsupported when evidence is insufficient.

This allows the same release to survive vendor changes when the required capability remains compatible.

## Notification failure model

Notification problems are classified into distinct layers:

```text
remote service
    |
push transport / FCM
    |
Google Play services
    |
Xiaomi process control
    |
target app execution
    |
Android notification channel
    |
HyperOS presentation / heads-up / lockscreen
```

A healthy FCM connection does not prove that a target app can execute, and a posted notification does not prove that heads-up presentation is enabled.

## Rule packs

Future compatibility rule packs may describe known capabilities and safe predefined actions.

Rule packs must not contain arbitrary executable commands. The app interprets a fixed schema and rejects unknown operations.

Required properties:

- signed;
- versioned;
- schema validated;
- atomic activation;
- rollback to the previous known-good pack.

## Data handling

The default diagnostic path is local.

If voluntary diagnostic sharing is introduced, raw notification content, credentials, financial data, stable device identifiers, and user account information remain out of scope.
