# Gemini development connection

Run `python backend/server.py` from the project folder. The debug Android emulator
uses `http://10.0.2.2:8787/analyze`. For a USB-connected phone, run
`adb reverse tcp:8787 tcp:8787` and change the debug URL to `http://127.0.0.1:8787/analyze`.

Credentials are read from `backend/.env` (ignored by Git) or environment variables:
`GEMINI_API_KEY` and `GEMINI_MODEL`. The configured model is `gemini-3.6-flash`.
The key never enters the APK. The server listens only on the local computer.

The app sends the ten actual question prompts, selected answer labels, typed answers,
academic grades/preferences and available course catalog. Name, email and school
are excluded. Gemini returns a summary and ordered catalog course IDs. Both server
and Android reject unknown or duplicate IDs. Existing course scores remain locally
calculated; the Gemini ordering and summary are displayed after successful analysis.
Failures show Retry and do not masquerade as Gemini results.

This is a local development server. A release build needs a deployed HTTPS backend,
authentication and request limits, and its `GEMINI_ANALYSIS_URL` configured before use.
Do not expose this development server directly to the internet.

Run `python -m unittest discover -s backend -p 'test_*.py'` for backend checks.
