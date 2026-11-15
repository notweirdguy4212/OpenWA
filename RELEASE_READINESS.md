# Release readiness status

This source bundle adds a real server-side OpenAI-compatible model gateway and the deployment scaffolding.
It is **not certified public-release-ready**: no production provider credential, deployed service, signing key,
Google Play account, physical-device QA, or CI runner is available in this environment.

## AI gateway

The backend uses the OpenAI Chat Completions API with model configurable via `OPENAI_MODEL` (default `gpt-4o-mini`).
The provider key stays on the server. It supports bounded in-process conversation memory (24 messages, 24-hour TTL),
request size validation, bearer token authentication, rate limiting, Helmet headers, and generic error responses.

Deploy `backend/` to a trusted HTTPS host, set environment variables from `.env.example`, and configure the app's
AI gateway endpoint to `https://YOUR_HOST/v1/chat`. The Android client currently posts `messages` and expects
`{"reply":"..."}`; for auth, update the Android request to attach `Authorization: Bearer <app token>` using a
secure provisioned token. Do not ship a shared production token embedded in a public APK; use per-install auth
and rotation in a production deployment.

## Release blockers / honest platform limits

- WhatsApp and other third-party VoIP call audio/control cannot be accessed by a normal Android app. This cannot
  be implemented by the Android `CallScreeningService`; do not advertise WhatsApp live-call listening or AI speech.
- Android's supported call screening flow only screens carrier calls and user-configured blocklist rejection.
  AI response suggestions are drafts, not auto-spoken audio.
- Spam scoring in the Android app is heuristic. A genuine global reputation API requires a selected vendor,
  account credentials, privacy review, and network integration. No such vendor key was supplied.
- Crash reporting (e.g. Firebase Crashlytics) requires a Firebase project and app registration; no project config
  was provided. Do not add fabricated Firebase IDs or claim telemetry is active.
- Memory in this starter backend is ephemeral and single-instance; production needs encrypted durable storage,
  retention controls, account-bound identity, and deletion workflows.
- The app needs a secure per-install authentication flow before public deployment. Never hardcode a shared API token.
- No Android SDK/Gradle build or device run has been performed in this environment. Run unit/instrumentation tests,
  lint, verify manifest/permissions, test call-role consent and Android versions, and configure Play signing before
  release.

## Local backend smoke test

```sh
cd backend
cp .env.example .env
# Set real secret values in your shell or deployment environment
npm install
npm start
curl http://localhost:8080/health
```
