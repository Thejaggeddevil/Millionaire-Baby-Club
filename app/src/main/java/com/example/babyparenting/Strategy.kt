package com.example.babyparenting.data.model

import com.google.gson.annotations.SerializedName

// ===== STRATEGY MODEL =====

data class Strategy(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("age_min")
    val age_min: Int = 0,

    @SerializedName("age_max")
    val age_max: Int = 0,

    @SerializedName("icon")
    val icon: String? = null,

    @SerializedName("completed_count")
    val completed_count: Int = 0,

    @SerializedName("total_activities")
    val total_activities: Int = 0
)

// ===== ACTIVITY MODEL =====

data class Activity(
    @SerializedName("id")
    val id: Int,

    @SerializedName("strategy_id")
    val strategy_id: Int = 0,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("description")
    val description: String = "",

    @SerializedName("plan")
    val plan: String = "",

    @SerializedName("do")          // ← YEH FIX KIYA, "do_instruction" se "do" kiya
    val do_instruction: String = "",

    @SerializedName("review")
    val review: String = "",

    @SerializedName("level")
    val level: Int = 1,

    @SerializedName("duration_minutes")
    val duration_minutes: Int = 10,

    @SerializedName("materials")
    val materials: String? = null
)

// ===== ACTIVITY COMPLETION REQUEST =====

data class ActivityCompletion(
    @SerializedName("user_id")
    val user_id: String,

    @SerializedName("activity_id")
    val activity_id: Int,

    @SerializedName("completed_at")
    val completed_at: Long? = null
)

// ===== ACTIVITY COMPLETION RESPONSE =====

data class ActivityCompletionResponse(
    @SerializedName("status")
    val status: String = "",

    @SerializedName("message")
    val message: String? = null
)

// ===== ACTIVITY DETAIL =====

data class ActivityDetail(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("plan")
    val plan: String = "",

    @SerializedName("do")
    val `do`: String = "",

    @SerializedName("review")
    val review: String = "",

    @SerializedName("strategy_id")
    val strategy_id: Int = 0
)

// ===== DAILY ACTIVITY RESPONSE =====

data class DailyActivityResponse(
    @SerializedName("activity")
    val activity: ActivityDetail? = null,

    @SerializedName("message")
    val message: String? = null
)

// ===== PROGRESS SUMMARY =====

data class ProgressSummary(
    @SerializedName("total_activities")
    val total_activities: Int = 0,

    @SerializedName("completed_activities")
    val completed_activities: Int = 0,

    @SerializedName("completion_percentage")
    val completion_percentage: Float = 0f,

    @SerializedName("current_level")
    val current_level: Int = 1
)

// ===== ACTIVITY WITH STATUS (LOCAL) =====

data class ActivityWithStatus(
    val activity: Activity,
    val isCompleted: Boolean
)