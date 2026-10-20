# JuaFit website

A standalone, website with Vercel serverless chat in JuaFit blue, gold, ivory and navy. Includes responsive light/dark themes, a six-step Kenyan CBE interest explorer, the nine-step 8-4-4 course explorer, pathway and track details, course progression, at least five employment areas per course, browser-local saved plans, printing and conversational JuaGuide with transcript uploads. Includes app-style registration and login. No Android build needed. Set Vercel Root Directory to `website`.

## Local preview

Requires Node.js 20 or later. No packages to install.

```
npm run dev
```

Open http://127.0.0.1:4173. To check the production output: `npm run build`, then `node serve.mjs --dist`.

## Vercel

Import this repository and set **Root Directory** to `website`. If you create a separate repository from the extracted ZIP, leave Root Directory at the repository root instead. Use **Other** framework, **Build Command** `npm run build`, **Output Directory** `dist`. The included vercel.json already declares build and output settings. Redeploy after changing environment variables.

Optional public variable: `PUBLIC_APP_URL=https://your-app-download-page.example` adds a download link in the footer. Without it, the link is omitted. No variable is required to run the site.

**Keep Gemini keys and Firebase Admin service-account credentials on the server.** Frontend variables are public. Firebase Web configuration is intentionally public and can be used by the browser SDK; it is different from a Gemini secret or an Admin private key.

## Registration and login

Without Firebase configuration, the website uses clearly labelled local preview accounts. Registration collects name, email, password and confirmation; login checks the preview account; sign-out clears its current-tab session. Use a sample password. A salted PBKDF2 verifier is stored locally instead of the original password, but this is only a frontend simulation and must never be trusted to protect server resources. Saved plans are separated by account in browser storage. Password reset emails are unavailable in preview mode.

To switch to real Firebase Authentication, enable Email/Password in Firebase Console and register a Web app. Set these four **public Web configuration** variables in Vercel and redeploy:

- `PUBLIC_FIREBASE_API_KEY`
- `PUBLIC_FIREBASE_AUTH_DOMAIN`
- `PUBLIC_FIREBASE_PROJECT_ID`
- `PUBLIC_FIREBASE_APP_ID`

Use values from the Firebase Web app's firebaseConfig object, not Android google-services.json or an Admin service-account file. Authorise the deployed website's domain in Firebase Authentication settings. When all four values are present, the app uses Firebase's browser SDK for account creation, login, reset emails and session handling. With none set, preview mode remains active. A partially configured build fails with a clear setup error.

For local live-Firebase testing, set the environment variables, run `npm run build`, then `node serve.mjs --dist`. The source preview (`npm run dev`) reads the local public `config.js` instead. SDK modules load from Firebase's official CDN only when real Firebase is configured.

Live AI still requires a separate protected Vercel endpoint. JuaGuide currently offers preset quick guidance, not live AI.

## Validation

```
npm test
npm run build
```

## Guidance and data

Pathway and track structure references: [KICD Grade 11 designs](https://kicd.ac.ke/cbc-materials/curriculum-designs/grade-eleven/) and the [Ministry of Education senior-school selection portal](https://selection.education.go.ke/). Subject names are examples, not approved combinations. CBE uses interest signals; it does not calculate official KJSEA placement scores. Equal interest signals remain equal, with a clear tie note; there are no fabricated percentages.

8-4-4 course descriptions and careers reuse the app's general guidance catalog. University offerings, eligibility and cutoffs are not asserted. The site links to KUCCPS for official admission information.

Assessment answers stay in memory. Theme preferences, account-specific plan names/IDs, and local preview account details/verifiers are stored locally. Preview sessions are scoped to the browser tab. Fonts load from Google Fonts with system fallbacks. No analytics. Preview authentication does not submit data externally; real Firebase authentication sends credentials to Firebase when configured.

## Files

- `index.html`, `styles.css`: accessible page layout, responsive styles and motion (with reduced-motion support).
- `app.js`: UI, dialogs, assessment navigation and browser storage.
- `guidance.js`: questions, local interest matching and career detail content.
- `catalog.js`: general course and career catalog.
- `assets/`: existing JuaFit emblem and learner illustration.
- `build.mjs`: static build; only allowlisted public configuration is emitted.

To upload without Git, use the supplied ZIP with Vercel CLI from its extracted directory (`vercel`), or commit the `website` directory to a repository and import it in Vercel.


JuaGuide now supports typed chat and PDF/image academic results (3 MB maximum). On Vercel set server-only GEMINI_API_KEY and GEMINI_MODEL, then redeploy. The API runs at /api/chat; never put the provider key in PUBLIC variables. The static local preview cannot run Python serverless functions. Messages and optional results are sent to the guidance service when Send is pressed. Firebase authentication and server-enforced premium limits still need production integration.
