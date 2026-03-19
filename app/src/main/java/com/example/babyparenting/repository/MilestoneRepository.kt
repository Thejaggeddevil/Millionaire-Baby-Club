package com.example.babyparenting.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.babyparenting.data.local.AdminMilestoneStore
import com.example.babyparenting.data.local.DatasetLoader
import com.example.babyparenting.data.model.AgeGroup
import com.example.babyparenting.data.model.DatasetSource
import com.example.babyparenting.data.model.JourneyProgress
import com.example.babyparenting.data.model.Milestone
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MilestoneRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("journey_progress", Context.MODE_PRIVATE)
    private val gson       = Gson()
    private val loader     = DatasetLoader(context)
    private val adminStore = AdminMilestoneStore(context)

    private var csvMilestones: List<Milestone> = emptyList()
    private var allMilestones: List<Milestone> = emptyList()

    private val _milestones = MutableStateFlow<List<Milestone>>(emptyList())
    private val _ageGroups  = MutableStateFlow<List<AgeGroup>>(emptyList())
    private val _progress   = MutableStateFlow(JourneyProgress(0, 0, 0))
    private val _isLoading  = MutableStateFlow(false)
    private val _error      = MutableStateFlow<String?>(null)

    val milestones: StateFlow<List<Milestone>> = _milestones.asStateFlow()
    val ageGroups:  StateFlow<List<AgeGroup>>  = _ageGroups.asStateFlow()
    val progress:   StateFlow<JourneyProgress> = _progress.asStateFlow()
    val isLoading:  StateFlow<Boolean>          = _isLoading.asStateFlow()
    val error:      StateFlow<String?>          = _error.asStateFlow()

    // ── Load ──────────────────────────────────────────────────────────────────

    suspend fun loadAll() {
        try {
            _isLoading.value = true
            _error.value     = null
            _ageGroups.value = loader.getAgeGroups()
            csvMilestones    = loader.loadInitialMilestones()
            mergeAndEmit()
        } catch (e: Exception) {
            _error.value = "Failed to load datasets: ${e.localizedMessage}"
        } finally {
            _isLoading.value = false
        }
    }

    fun refreshAdminMilestones() = mergeAndEmit()

    // ── Completion ────────────────────────────────────────────────────────────

    fun toggleCompletion(id: String) {
        val ids = getCompletedIds().toMutableSet()
        if (id in ids) ids.remove(id) else ids.add(id)
        saveCompletedIds(ids)
        // ✅ .toList() — forces new list reference so StateFlow triggers collect
        allMilestones     = allMilestones.map { it.copy(isCompleted = it.id in ids) }.toList()
        _milestones.value = allMilestones
        _progress.value   = buildProgress()
    }

    fun setChildAge(months: Int) {
        prefs.edit().putInt(KEY_AGE, months).apply()
        val ids = getCompletedIds().toMutableSet()
        allMilestones.filter { it.ageMonths <= months }.forEach { ids.add(it.id) }
        saveCompletedIds(ids)
        allMilestones     = allMilestones.map { it.copy(isCompleted = it.id in ids) }.toList()
        _milestones.value = allMilestones
        _progress.value   = buildProgress()
    }

    fun setChildName(name: String) {
        prefs.edit().putString(KEY_NAME, name).apply()
        _progress.value = _progress.value.copy(childName = name)
    }

    fun getChildAgeMonths(): Int = prefs.getInt(KEY_AGE, 0)
    fun getChildName(): String   = prefs.getString(KEY_NAME, "") ?: ""

    // ── Private ───────────────────────────────────────────────────────────────

    private fun mergeAndEmit() {
        val completed = getCompletedIds()
        val adminMs   = adminStore.getAll().map { am ->
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

        allMilestones = (csvMilestones.map { it.copy(isCompleted = it.id in completed) } + adminMs)
            .sortedWith(compareBy({ it.ageMonths }, { it.source.ordinal }))
            .toList()  // ✅ fresh list

        _milestones.value = allMilestones
        _progress.value   = buildProgress()
    }

    private fun buildProgress() = JourneyProgress(
        totalMilestones     = allMilestones.size,
        completedMilestones = allMilestones.count { it.isCompleted },
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