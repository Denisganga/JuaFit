# Holistic course scoring pilot v1

Status: initial configuration, not professionally calibrated or psychometrically validated.
This change applies to 8-4-4 course suitability, not CBE pathway scoring.

| Factor | Weight | Independent evidence |
|---|---:|---|
| Academic readiness | 25% | KCSE mean grade (estimated from supplied grades only when mean is absent), used once |
| Subject relevance | 15% | Separate subject-enjoyment selections; not a second use of grades |
| Interests | 15% | Area-of-interest question; not inferred from school marks or self-rated strengths |
| Aptitude | 20% | Separate externally scored aptitude evidence per course; currently absent in the UI |
| Career goals | 15% | Goal question; Android additionally accepts explicitly chosen career goals |
| Preferences | 10% | Environment preferences; Android additionally supports explicit profile preferences |

Self-rated skills, optional notes and values do not silently substitute for aptitude or get counted again as interest/goal evidence. Shared tag names may occur in different independent questions, but each question belongs to one scoring factor. Generic academic readiness does not establish course admission eligibility. Subject enjoyment does not establish subject-grade eligibility.

Course factors with missing evidence are omitted, not assigned zero. Observed zero aptitude remains zero. Weights of observed factors are normalised to sum to one. Unknown aptitude for a particular course is missing for that course. No interest or aptitude evidence means no displayed suitability percentage, even when grades are available.

Results list outstanding assessments and remain provisional. Self-reported skills never complete aptitude. There is no separate scored aptitude test in the current app; results disclose this rather than inventing completion. The current interest question is exploratory and not a validated psychometric instrument.

Configuration: Android EightFourFourScoringWeights constructor; website courseWeights and rankCourses weights argument. Invalid negative/nonfinite/all-zero weights are rejected. Assessment-cache version changes with this scoring revision.

## Evidence and refinement

Before promoting this pilot as validated, collect consented, anonymised cases spanning strong/average/low grades, different subject combinations, incomplete profiles and contrasting interests. Ask qualified career guidance professionals to independently review the top five and their factor breakdowns. Track relevance, missing evidence, surprising recommendations, learner understanding and disagreements across academic backgrounds. Keep academic eligibility review separate from interest suitability.

Record: anonymous case ID, scoring version, available factors, top-five IDs/scores, reviewer role, relevance comments, learner feedback, proposed adjustment and reason. Do not store names, transcripts or private grades in a public feedback log.

Compare candidate weights on held-out profiles, document tradeoffs, and only release a new scoring version after reviewer feedback. Keep a changelog and rerun missing-data, zero-score, bounded-score, normalisation, disjoint-evidence and contrasting-profile tests. These automated tests verify implementation, not career-outcome validity.
