package com.pathwayfit.app.core.model

/**
 * The shared vocabulary of interest/strength "tags" that connects assessment answers
 * to CBE tracks/areas, courses and careers. Tags are PATHWAYFIT's own matching
 * vocabulary — they are not official CBE or KUCCPS categories.
 */
object TagCatalog {
    private val labels = mapOf(
        "science" to "Science", "maths" to "Mathematics", "technology" to "Technology",
        "computing" to "Computing & coding", "data" to "Data & numbers", "engineering" to "Engineering",
        "building" to "Building & construction", "mechanics" to "Machines & mechanics",
        "electrical" to "Electricity & electronics", "aviation" to "Aviation", "marine" to "Marine life & water",
        "media" to "Media production", "agriculture" to "Agriculture & farming", "nature" to "Nature & environment",
        "health" to "Health & the human body", "care" to "Caring for others", "food" to "Food & nutrition",
        "languages" to "Languages", "writing" to "Writing", "literature" to "Literature & stories",
        "communication" to "Communication", "culture" to "Culture & heritage", "society" to "Society & communities",
        "history" to "History", "geography" to "Geography & places", "faith" to "Faith & values",
        "business" to "Business", "money" to "Money & finance", "law" to "Law & justice",
        "leadership" to "Leadership", "people" to "Working with people",
        "arts" to "The arts", "performing" to "Performing", "music" to "Music", "drama" to "Drama & theatre",
        "film" to "Film & video", "visual" to "Visual art", "design" to "Design", "crafts" to "Crafts & making",
        "sports" to "Sports", "fitness" to "Fitness & movement", "coaching" to "Coaching & training others",
        "analytical" to "Analytical thinking", "creative" to "Creativity", "practical" to "Practical skills",
        "problem_solving" to "Problem solving", "teamwork" to "Teamwork", "independent" to "Independent work",
        "outdoor" to "Working outdoors", "hands_on" to "Hands-on learning", "research" to "Research & investigation",
        "helping" to "Helping others", "organising" to "Organising & planning", "entrepreneurial" to "Starting things",
        "discussion" to "Discussion & debate", "visual_learning" to "Learning by seeing",
    )

    fun label(tag: String): String = labels[tag] ?: tag.replace('_', ' ').replaceFirstChar { it.uppercase() }

    data class ProfileOption(val id: String, val label: String, val tags: List<String>)

    val interestOptions = listOf(
        ProfileOption("i_science", "Science & experiments", listOf("science", "research")),
        ProfileOption("i_maths", "Mathematics & puzzles", listOf("maths", "analytical")),
        ProfileOption("i_tech", "Computers & technology", listOf("technology", "computing")),
        ProfileOption("i_build", "Building & fixing things", listOf("engineering", "building", "mechanics")),
        ProfileOption("i_nature", "Nature, animals & farming", listOf("nature", "agriculture")),
        ProfileOption("i_health", "Health & medicine", listOf("health", "care")),
        ProfileOption("i_lang", "Languages & reading", listOf("languages", "literature")),
        ProfileOption("i_society", "History & how society works", listOf("history", "society")),
        ProfileOption("i_business", "Business & money", listOf("business", "money")),
        ProfileOption("i_arts", "Art & design", listOf("visual", "design")),
        ProfileOption("i_music", "Music, dance & drama", listOf("music", "performing", "drama")),
        ProfileOption("i_sports", "Sports & fitness", listOf("sports", "fitness")),
        ProfileOption("i_media", "Media, film & content", listOf("media", "film")),
    )

    val strengthOptions = listOf(
        ProfileOption("s_analytical", "Analytical", listOf("analytical")),
        ProfileOption("s_creative", "Creative", listOf("creative")),
        ProfileOption("s_problem", "Problem solver", listOf("problem_solving")),
        ProfileOption("s_comm", "Good communicator", listOf("communication")),
        ProfileOption("s_lead", "Leader", listOf("leadership")),
        ProfileOption("s_team", "Team player", listOf("teamwork")),
        ProfileOption("s_practical", "Practical / hands-on", listOf("practical", "hands_on")),
        ProfileOption("s_organised", "Organised", listOf("organising")),
        ProfileOption("s_caring", "Caring & patient", listOf("care", "helping")),
        ProfileOption("s_physical", "Physically active", listOf("fitness", "sports")),
    )

    val activityOptions = listOf(
        ProfileOption("a_experiments", "Doing experiments", listOf("science", "research", "hands_on")),
        ProfileOption("a_coding", "Coding or using computers", listOf("computing", "technology")),
        ProfileOption("a_making", "Making and building models", listOf("crafts", "engineering", "hands_on")),
        ProfileOption("a_reading", "Reading and writing stories", listOf("literature", "writing")),
        ProfileOption("a_debate", "Debating and discussions", listOf("discussion", "communication")),
        ProfileOption("a_drawing", "Drawing and painting", listOf("visual", "creative")),
        ProfileOption("a_performing", "Singing, dancing, acting", listOf("performing", "music", "drama")),
        ProfileOption("a_sports", "Playing sports", listOf("sports", "teamwork")),
        ProfileOption("a_farming", "Gardening / farming projects", listOf("agriculture", "outdoor")),
        ProfileOption("a_selling", "Selling or running a small project", listOf("entrepreneurial", "business")),
        ProfileOption("a_helping", "Helping and caring for others", listOf("helping", "care")),
        ProfileOption("a_fieldwork", "Field trips and exploring places", listOf("geography", "outdoor")),
    )

    fun tagsFor(optionIds: List<String>, options: List<ProfileOption>): List<String> =
        options.filter { it.id in optionIds }.flatMap { it.tags }

    val kenyanCounties = listOf(
        "Baringo", "Bomet", "Bungoma", "Busia", "Elgeyo-Marakwet", "Embu", "Garissa", "Homa Bay", "Isiolo",
        "Kajiado", "Kakamega", "Kericho", "Kiambu", "Kilifi", "Kirinyaga", "Kisii", "Kisumu", "Kitui", "Kwale",
        "Laikipia", "Lamu", "Machakos", "Makueni", "Mandera", "Marsabit", "Meru", "Migori", "Mombasa",
        "Murang'a", "Nairobi", "Nakuru", "Nandi", "Narok", "Nyamira", "Nyandarua", "Nyeri", "Samburu", "Siaya",
        "Taita-Taveta", "Tana River", "Tharaka-Nithi", "Trans Nzoia", "Turkana", "Uasin Gishu", "Vihiga",
        "Wajir", "West Pokot",
    )
}
