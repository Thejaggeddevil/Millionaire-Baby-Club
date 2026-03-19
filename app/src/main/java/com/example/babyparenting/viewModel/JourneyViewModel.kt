package com.example.babyparenting.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.babyparenting.data.model.AgeGroup
import com.example.babyparenting.data.model.DatasetSource
import com.example.babyparenting.data.model.JourneyProgress
import com.example.babyparenting.data.model.Milestone
import com.example.babyparenting.data.model.UiState
import com.example.babyparenting.data.repository.ApiRepository
import com.example.babyparenting.data.repository.MilestoneRepository
import com.example.babyparenting.network.api.RetrofitProvider
import com.example.babyparenting.network.model.AdviceResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class JourneyViewModel(app: Application) : AndroidViewModel(app) {

    private val milestoneRepo = MilestoneRepository(app)
    private val apiRepo       = ApiRepository(RetrofitProvider.babyApi)

    val milestones: StateFlow<List<Milestone>> = milestoneRepo.milestones
    val ageGroups:  StateFlow<List<AgeGroup>>  = milestoneRepo.ageGroups
    val progress:   StateFlow<JourneyProgress>  = milestoneRepo.progress
    val isLoading:  StateFlow<Boolean>           = milestoneRepo.isLoading
    val loadError:  StateFlow<String?>           = milestoneRepo.error

    private val _selectedMilestone = MutableStateFlow<Milestone?>(null)
    val selectedMilestone: StateFlow<Milestone?> = _selectedMilestone.asStateFlow()

    private val _adviceState = MutableStateFlow<UiState<AdviceResponse>>(UiState.Idle)
    val adviceState: StateFlow<UiState<AdviceResponse>> = _adviceState.asStateFlow()

    private val _activeFilter       = MutableStateFlow<DatasetSource?>(null)
    val activeFilter: StateFlow<DatasetSource?> = _activeFilter.asStateFlow()

    private val _visibleMilestones = MutableStateFlow<List<Milestone>>(emptyList())
    val visibleMilestones: StateFlow<List<Milestone>> = _visibleMilestones.asStateFlow()

    private val _filteredMilestones = MutableStateFlow<List<Milestone>>(emptyList())
    val filteredMilestones: StateFlow<List<Milestone>> = _filteredMilestones.asStateFlow()

    init {
        viewModelScope.launch { milestoneRepo.initialLoad() }

        viewModelScope.launch {
            combine(milestoneRepo.milestones, _activeFilter) { all, filter ->
                if (filter == null) all else all.filter { it.source == filter }
            }.collect { filtered ->
                _filteredMilestones.value = filtered
                _visibleMilestones.value  = computeVisible(filtered)
                checkAndLoadNextGroup(filtered)
            }
        }
    }

    // ── Completion — ONE WAY, no undo ─────────────────────────────────────────

    /**
     * Mark a milestone as complete. This is permanent — once done, cannot be undone.
     * Called from AdviceScreen's "Mark as Complete" button.
     */
    fun markComplete(id: String) {
        milestoneRepo.markComplete(id)   // one-way in repository
    }

    /**
     * Called from MilestoneCard's circle tap (also one-way).
     * If already complete — do nothing.
     */
    fun toggleCompletion(id: String) {
        milestoneRepo.markComplete(id)   // same as markComplete — no undo
    }

    // ── Visible logic — same age group, 4-4 ──────────────────────────────────

    private fun computeVisible(all: List<Milestone>): List<Milestone> {
        if (all.isEmpty()) return emptyList()

        val activeGroupId = findActiveGroupId(all) ?: return all

        val previousDone   = all.filter { it.ageGroupId < activeGroupId }
        val activeGroupMs  = all.filter { it.ageGroupId == activeGroupId }

        val visibleFromActive = mutableListOf<Milestone>()
        var batchStart = 0
        while (batchStart < activeGroupMs.size) {
            val end   = minOf(batchStart + 4, activeGroupMs.size)
            val batch = activeGroupMs.subList(batchStart, end)
            visibleFromActive.addAll(batch)
            if (!batch.all { it.isCompleted }) break
            batchStart += 4
        }

        return previousDone + visibleFromActive
    }

    private fun findActiveGroupId(all: List<Milestone>): Int? =
        all.map { it.ageGroupId }
            .distinct()
            .sorted()
            .firstOrNull { groupId ->
                all.filter { it.ageGroupId == groupId }.any { !it.isCompleted }
            }

    private fun checkAndLoadNextGroup(all: List<Milestone>) {
        val activeGroupId  = findActiveGroupId(all) ?: return
        val activeGroupMs  = all.filter { it.ageGroupId == activeGroupId }
        if (activeGroupMs.any { !it.isCompleted }) return
        viewModelScope.launch {
            milestoneRepo.loadNextGroupIfNeeded(activeGroupId + 1)
        }
    }

    // ── Lock logic ────────────────────────────────────────────────────────────

    fun isLocked(milestone: Milestone): Boolean {
        val visible       = _visibleMilestones.value
        val activeGroupId = findActiveGroupId(visible)
        if (milestone.ageGroupId != activeGroupId) return false

        val groupVisible = visible.filter { it.ageGroupId == activeGroupId }
        val idxInGroup   = groupVisible.indexOf(milestone)
        if (idxInGroup <= 0) return false

        val indexInBatch = idxInGroup % 4
        if (indexInBatch == 0) return false

        val prev = groupVisible.getOrNull(idxInGroup - 1) ?: return false
        return !prev.isCompleted
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    fun onMilestoneTapped(milestone: Milestone) {
        _selectedMilestone.value = milestone
        fetchAdvice(milestone)
    }

    fun toggleFilter(source: DatasetSource) {
        _activeFilter.value = if (_activeFilter.value == source) null else source
    }

    fun clearFilter()                    { _activeFilter.value = null }
    fun setChildAge(months: Int)         = milestoneRepo.setChildAge(months)
    fun setChildName(name: String)       = milestoneRepo.setChildName(name)
    fun getChildAgeMonths(): Int         = milestoneRepo.getChildAgeMonths()
    fun getChildName(): String           = milestoneRepo.getChildName()
    fun refreshAfterAdminEdit()          = milestoneRepo.refreshAdminMilestones()

    fun resetAdvice() {
        _adviceState.value       = UiState.Idle
        _selectedMilestone.value = null
    }

    fun retryAdvice() { _selectedMilestone.value?.let { fetchAdvice(it) } }

    fun reloadDatasets() {
        viewModelScope.launch { milestoneRepo.initialLoad() }
    }

    private fun fetchAdvice(milestone: Milestone) {
        viewModelScope.launch {
            _adviceState.value = UiState.Loading
            _adviceState.value = apiRepo.fetchAdvice(
                model = milestone.source.apiModel,
                query = milestone.apiQuery
            )
        }
    }
}