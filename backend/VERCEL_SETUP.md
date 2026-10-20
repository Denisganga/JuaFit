# Deploy JuaGuide to Vercel

## Add to the existing nita-pass project

Vercel environment variables alone do not install API code. Add these files to the Git repository connected to `nita-pass.vercel.app`, inside its configured Root Directory:

```
server.py
requirements.txt
api/chat.py
api/analyze.py
api/health.py
```

Keep existing website files, framework settings, API routes and package files. If `server.py` or any of these API filenames already exist, merge deliberately rather than overwrite them. If requirements.txt already exists, retain its existing dependencies (this backend adds no third-party dependencies).

Merge the following into the existing vercel.json. Keep existing functions and rewrites, and put these three rewrites BEFORE any catch-all website rewrite:

```json
{
  "functions": {
    "api/*.py": {"maxDuration": 120, "includeFiles": "server.py"}
  },
  "rewrites": [
    {"source": "/chat", "destination": "/api/chat"},
    {"source": "/analyze", "destination": "/api/analyze"},
    {"source": "/health", "destination": "/api/health"}
  ]
}
```

In Vercel → Project → Settings → Environment Variables, add:

| Name | Value |
| --- | --- |
| `GEMINI_API_KEY` | Your key, entered only in Vercel |
| `GEMINI_MODEL` | `gemini-3.6-flash` |

Enable the variables for Production (and Preview if you intend to test preview deployments). Do not use NEXT_PUBLIC_ or VITE_ prefixes. Push the files to the project's production branch and redeploy.

Open `https://nita-pass.vercel.app/health`. A JSON response with `status: configured` confirms the routes and environment variables are present. It does NOT prove the provider accepted the key. Next, run the included `verify.py` against that URL to test a real chat response. The Android app already points to this base URL; it does not need the secret key or a new URL.

The new standalone frontend website currently runs local guidance. Adding these server endpoints does not automatically switch that website to live AI; its UI would need to be connected separately.

## Standalone backend project (alternative)

1. Create a Vercel project with `backend` as its Root Directory and Other as the framework preset. No frontend build or output directory is required.
2. Add server environment variables in Project Settings → Environment Variables:
   - `GEMINI_API_KEY`: your provider API key.
   - `GEMINI_MODEL`: the model enabled for your provider account (currently configured as `gemini-3.6-flash`).
3. Deploy. Redeploy after changing environment variables. Never put the key in Android source, Gradle properties, a public repository, or the APK.
4. Send the production HTTPS URL back to the app developer. Rebuild the app with `-PJUA_BACKEND_URL=https://your-project.vercel.app`. The base URL must not include `/analyze`; the app appends `/analyze` or `/chat`.

The deployment exposes `/analyze` and `/chat`. Requests include the learner's assessment context. Transcript uploads support PDF, JPG, PNG and WebP, capped at 3 MB to allow for base64 encoding under Vercel's request limit. Files are passed to the provider without saving them to server disk.

`/api/chat`, `/api/analyze`, and `/api/health` are also available directly. GET `/health` checks configuration, POST `/chat` generates conversational guidance, and POST `/analyze` uses the app's ten-question assessment payload.

The phone no longer asks users for an API key. Until the production URL is configured, it displays a service-connection message.

Before opening the deployment to public users, add server-verified authentication, durable usage limits and payment entitlements. The current prototype uses local accounts and local premium state; those do not provide server-enforced billing protection. Keep Vercel deployment protection enabled during development.
