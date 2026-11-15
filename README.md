# CallCatcher 2.1 — Android source project

Local-first Android call screening with an optional, user-configured AI gateway.

## Implemented
- Android `ROLE_CALL_SCREENING` request flow; system approval remains mandatory.
- Local blocklist with canonical number formatting and input validation.
- Carrier-call screening service; only incoming calls explicitly on the user's blocklist are automatically rejected.
- On-device spam heuristics: explicit blocklist match, Android caller-number verification failure (API 30+), unusually short caller ID, and repeated-digit patterns.
- AI conversation screen that sends chat messages to a user-configured HTTPS backend using a documented JSON contract.
- Unit tests for number normalization, blocklist matching, and conservative spam assessment.

## AI gateway contract

Configure an HTTPS endpoint in the AI tab. The app sends:

```json
{
  "messages": [
    {"role": "assistant", "content": "…"},
    {"role": "user", "content": "…"}
  ]
}
```

The endpoint must return:

```json
{"reply": "Your assistant response"}
```

The app intentionally does not embed an OpenAI or other provider API key. Deploy a small authenticated backend/proxy, keep provider credentials there, and point the app at its HTTPS endpoint. The gateway must validate requests, enforce rate limits, and avoid logging sensitive caller data.

## Platform limits — important

- Android `CallScreeningService` handles supported carrier/mobile telephone calls only. It cannot intercept WhatsApp, Signal, Telegram, or other third-party VoIP calls.
- A normal Android app cannot tap into WhatsApp live-call audio, transcribe it, answer it, or inject synthesized speech into that call. This build therefore does not claim to provide WhatsApp live-call assistance or an AI voice agent.
- AI chat is a real backend integration point, but it is not active until you deploy and configure a compatible HTTPS gateway.
- Spam scoring is heuristic and local, not a global reputation feed. A risk score alone never auto-blocks a caller; only the user's explicit blocklist does.
- No call recording or background microphone access is included.

## Build and test

Requirements: Android Studio, JDK 17, Android SDK Platform 35, and network access for Gradle dependency resolution.

```bash
gradle :app:testDebugUnitTest
gradle :app:assembleDebug
```

Then install on an Android 10+ device, grant the call-screening role through Android's system dialog, add a controlled test number, and verify both blocked and unblocked calls.

## Not verified in this environment

This source update was prepared without an installed Gradle executable or Android SDK in the build environment. Run the commands above in Android Studio/CI before distributing an APK. Device-level call-screening behavior must be tested on supported Android hardware.
