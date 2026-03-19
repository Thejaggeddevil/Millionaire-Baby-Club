package com.example.babyparenting.data.model

data class ParentGuide(
    val id: String,
    val ageRange: String,
    val domain: String,
    val skillName: String,
    val learningGoal: String,
    val whyItMatters: String,
    val howToTeach: String,
    val dos: List<String>,
    val donts: List<String>,
    val tip: String,
    val ageGroupLabel: String   // "2–5 Years" or "5–12 Years"
)
