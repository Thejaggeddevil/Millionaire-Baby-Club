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

    private val _filteredMilestones = MutableStateFlow<List<Milestone>>(emptyList())
    val filteredMilestones: StateFlow<List<Milestone>> = _filteredMilestones.asStateFlow()

    init {
        viewModelScope.launch { milestoneRepo.loadAll() }
        viewModelScope.launch {
            combine(milestoneRepo.milestones, _activeFilter) { all, filter ->
                if (filter == null) all else all.filter { it.source == filter }

            }.collect { _filteredMilestones.value = it }
        }
    }

    fun onMilestoneTapped(milestone: Milestone) {
        _selectedMilestone.value = milestone
        fetchAdvice(milestone)
    }

    fun toggleFilter(source: DatasetSource) {
        _activeFilter.value = if (_activeFilter.value == source) null else source
    }

    fun clearFilter()                    { _activeFilter.value = null }
    fun toggleCompletion(id: String)     = milestoneRepo.toggleCompletion(id)
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

    fun reloadDatasets() { viewModelScope.launch { milestoneRepo.loadAll() } }

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