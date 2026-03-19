package com.example.babyparenting.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.babyparenting.data.local.AdminMilestoneStore
import com.example.babyparenting.data.local.LazyDatasetLoader
import com.example.babyparenting.data.model.AgeGroup
import com.example.babyparenting.data.model.DatasetSource
import com.example.babyparenting.data.model.JourneyProgress
import com.example.babyparenting.data.model.Milestone
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Single source of truth. Lazy loads one age group at a time.
 * markComplete is ONE-WAY — completed milestones cannot be un-completed.
 */
class MilestoneRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("journey_progress", Context.MODE_PRIVATE)
    private val gson       = Gson()
    private val loader     = LazyDatasetLoader(context)
    private val adminStore = AdminMilestoneStore(context)

    private var loadedMilestones: MutableList<Milestone> = mutableListOf()
    private var highestLoadedGroup: Int = 0

    private val _milestones = MutableStateFlow<List<Milestone>>(emptyList())
    private val _ageGroups  = MutableStateFlow<List<AgeGroup>>(emptyList())
    private val _progress   = MutableStateFlow(JourneyProgress(0, 0, 0))
    private val _isLoading  = MutableStateFlow(false)
    private val _error      = MutableStateFlow<String?>(null)

    val milestones: StateFlow<List<Milestone>> = _milestones.asStateFlow()
    val ageGroups:  StateFlow<List<AgeGroup>>  = _ageGroups.asStateFlow()
    val progress:   StateFlow<JourneyProgress>  = _progress.asStateFlow()
    val isLoading:  StateFlow<Boolean>           = _isLoading.asStateFlow()
    val error:      StateFlow<String?>           = _error.asStateFlow()

    // ── Load ──────────────────────────────────────────────────────────────────

    suspend fun initialLoad() {
        try {
            _isLoading.value = true
            _error.value     = null
            _ageGroups.value = loader.getAgeGroups()

            val childAge   = getChildAgeMonths()
            val startGroup = loader.startingGroupId(childAge)

            for (groupId in 1..startGroup) {
                loadGroupIfNeeded(groupId)
            }

            // CRITICAL FIX: After loading, recalculate completed IDs from scratch
            // based on the stored child age. Do NOT blindly read old SharedPreferences
            // data — that causes stale completions from previous test sessions to persist.
            //
            // Rule: milestones STRICTLY BEFORE child's age are auto-completed.
            // Milestones AT or AFTER child's age are NOT completed.
            //
            // Newborn (0 months) → ageMonths < 0 → nothing → 0 completions ✅
            // 6 months → ageMonths < 6 → 0–5 month milestones auto-completed ✅
            val correctIds = loadedMilestones
                .filter { it.ageMonths < childAge }
                .map { it.id }
                .toSet()

            // Merge: keep any IDs the user manually completed AND the age-based ones.
            // This preserves real user progress (milestones they tapped Mark as Complete).
            val savedIds    = getCompletedIds()
            val validSaved  = savedIds.filter { id ->
                loadedMilestones.any { it.id == id }
            }.toSet()

            val finalIds = (correctIds + validSaved).toSet()
            saveCompletedIds(finalIds)

            mergeAndEmitWithIds(finalIds)
        } catch (e: Exception) {
            _error.value = "Failed to load: ${e.localizedMessage}"
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun loadNextGroupIfNeeded(nextGroupId: Int) {
        if (nextGroupId > loader.totalGroups()) return
        if (nextGroupId <= highestLoadedGroup) return
        try {
            _isLoading.value = true
            loadGroupIfNeeded(nextGroupId)
            mergeAndEmit()
        } catch (e: Exception) {
            // silent fail
        } finally {
            _isLoading.value = false
        }
    }

    // ── ONE-WAY completion ────────────────────────────────────────────────────

    /**
     * Mark a milestone as complete. PERMANENT — cannot be reversed.
     * Once a milestone is in completedIds, it stays there forever.
     */
    fun markComplete(id: String) {
        val ids = getCompletedIds().toMutableSet()
        if (id in ids) return   // already complete — do nothing
        ids.add(id)
        saveCompletedIds(ids)
        loadedMilestones = loadedMilestones.map {
            it.copy(isCompleted = it.id in ids)
        }.toMutableList()
        _milestones.value = loadedMilestones.toList()
        _progress.value   = buildProgress()
    }

    // ── Child profile ─────────────────────────────────────────────────────────

    fun setChildAge(months: Int) {
        prefs.edit().putInt(KEY_AGE, months).apply()

        // RESET completedIds and recompute from scratch.
        // This ensures changing age always gives a clean, correct state.
        // We use STRICT less-than (<) so milestones AT the child's current age
        // are NOT auto-completed — those are the ones they should do NOW.
        //
        // Example: Newborn (0 months) → nothing auto-completed (0 < 0 = false)
        // Example: 6 months → milestones for 0–5 months auto-completed (ageMonths < 6)
        val freshIds = loadedMilestones
            .filter { it.ageMonths < months }   // strictly BEFORE child's age
            .map { it.id }
            .toMutableSet()

        saveCompletedIds(freshIds)
        loadedMilestones = loadedMilestones.map {
            it.copy(isCompleted = it.id in freshIds)
        }.toMutableList()
        _milestones.value = loadedMilestones.toList()
        _progress.value   = buildProgress()
    }

    fun setChildName(name: String) {
        prefs.edit().putString(KEY_NAME, name).apply()
        _progress.value = _progress.value.copy(childName = name)
    }

    fun getChildAgeMonths(): Int = prefs.getInt(KEY_AGE, 0)
    fun getChildName(): String   = prefs.getString(KEY_NAME, "") ?: ""

    fun refreshAdminMilestones() = mergeAndEmit()

    // ── Private ───────────────────────────────────────────────────────────────

    private suspend fun loadGroupIfNeeded(groupId: Int) {
        if (groupId <= highestLoadedGroup) return
        val newMs = loader.loadForGroup(groupId)
        loadedMilestones.addAll(newMs)
        highestLoadedGroup = groupId
    }

    private fun mergeAndEmit() {
        val completed = getCompletedIds()
        mergeAndEmitWithIds(completed)
    }

    private fun mergeAndEmitWithIds(completed: Set<String>) {
        val adminMs = adminStore.getAll().map { am ->
            Milestone(
                id           = am.id,
                title        = am.title,
                subtitle     = am.subtitle,
                domain       = am.domain,
                ageMonths    = am.ageMonths,
                ageRange     = am.ageRange,
                ageGroupId   = am.ageGroupId,
                source       = DatasetSource.ADMIN_CUSTOM,
                apiQuery     = am.apiQuery,
                iconEmoji    = am.iconEmoji,
                accentColor  = DatasetSource.ADMIN_CUSTOM.colorHex,
                isCompleted  = am.id in completed,
                isAdminAdded = true
            )
        }

        loadedMilestones = loadedMilestones.map {
            it.copy(isCompleted = it.id in completed)
        }.toMutableList()

        val merged = (loadedMilestones + adminMs)
            .sortedWith(compareBy({ it.ageMonths }, { it.source.ordinal }))
            .toList()

        _milestones.value = merged
        _progress.value   = buildProgress()
    }

    private fun buildProgress() = JourneyProgress(
        totalMilestones     = loadedMilestones.size,
        completedMilestones = loadedMilestones.count { it.isCompleted },
        childAgeMonths      = getChildAgeMonths(),
        childName           = getChildName()
    )

    private fun getCompletedIds(): Set<String> {
        val json = prefs.getString(KEY_COMPLETED, null) ?: return emptySet()
        return try {
            val type = object : TypeToken<Set<String>>() {}.type
            gson.fromJson(json, type) ?: emptySet()
        } catch (e: Exception) { emptySet() }
    }

    private fun saveCompletedIds(ids: Set<String>) {
        prefs.edit().putString(KEY_COMPLETED, gson.toJson(ids)).apply()
    }

    companion object {
        private const val KEY_COMPLETED = "completed_ids"
        private const val KEY_AGE       = "child_age_months"
        private const val KEY_NAME      = "child_name"
    }
}