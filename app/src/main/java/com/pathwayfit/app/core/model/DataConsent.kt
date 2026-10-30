package com.pathwayfit.app.core.model

object DataConsent {
    const val VERSION = "2026-10-30"
    val sections = listOf(
        "What you share" to "JuaFit uses your name and email to create your account. Profile details, academic results, interests, goals and assessment answers are used to personalise course and pathway guidance and save your progress.",
        "JuaGuide and optional uploads" to "When you request AI guidance, your messages and relevant assessment or profile details are sent to the online JuaGuide service and its AI processing providers. Academic documents are sent only when you choose to upload them. Uploads are optional. Avoid including identification numbers, financial details or information about other people.",
        "Storage and caching" to "Accounts, profiles and saved plans in the current local version stay on your device or browser. When cloud sign-in is enabled, account details are processed by the sign-in service. Public course information may be cached for seven days on your device and 24 hours on the server. Website assessment responses may be reused for up to 24 hours within your browser session. Local records remain until removed; signing out alone does not delete them.",
        "Your choice" to "Registration requires your agreement to the data use described here. You may decline by not registering. You can use assessments without uploading a document. To stop future AI submissions, stop using JuaGuide and sign out. Clearing app or browser data removes local records, but does not delete information already submitted to online services. Contact the JuaFit service administrator for questions or requests about information already sent online.",
        "Learners and guardians" to "If you are under 18, review this notice with a parent or guardian before creating an account or sharing academic documents.",
        "Guidance limits" to "Recommendations and suitability percentages are guidance, not admission or employment guarantees. Only programme listings and printed requirements identified with a KUCCPS source are verified against that edition.",
        "Agreement record" to "When you register, JuaFit records the notice version and acceptance time with your local account. Opening this notice does not tick the consent box. This agreement does not authorise marketing or public sharing of your documents."
    )
}
