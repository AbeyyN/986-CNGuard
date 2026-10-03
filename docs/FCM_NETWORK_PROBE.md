# Opt-in FCM network reachability probe

A regular Android app can check TCP connectivity without Shizuku, root, Usage Access, or Notification Access.

The current implementation tries a TCP handshake only to `mtalk.google.com` on ports 5228, 5229, 5230 and 443. Google lists these ports and this hostname in its [FCM network configuration guidance](https://firebase.google.com/docs/cloud-messaging/network-configuration).

The probe:

- runs only when the user explicitly presses **Test Google Push Network**;
- uses the phone's default network route; repeat manually on Wi-Fi and mobile data if required;
- resolves the hardcoded Google hostname and checks up to two resolved addresses;
- sends no Firebase token, notification payload, message body or HTTP request;
- reports only reachable port numbers or the absence of an observed connection, without retaining IP addresses.

## Interpretation

- **TCP reachable:** at least one handshake completed; this does not prove that Google Play services has an active FCM session, that a third-party app is registered or that a push arrived.
- **No TCP connection observed:** none of the attempted connections completed; DNS/IPv6 routing, VPN, mobile filtering, firewall policy or an individual endpoint may be involved. This does not prove Google FCM is globally blocked.
- **DNS unavailable:** the hostname could not be resolved; no port determination can be made.

Actual remote push delivery and latency require a separate, authenticated, consent-based end-to-end FCM Lab. This network probe deliberately does not simulate such a result.

Privacy: by opting in, the user's phone opens ordinary TCP connection attempts to Google. Google or the network provider may observe the source network IP as a normal consequence of connectivity checks. CN Guard does not retain resolved addresses or operate a backend for this test.
