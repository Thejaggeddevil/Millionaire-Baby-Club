package com.example.babyparenting.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.babyparenting.data.local.ParentGuideLoader
import com.example.babyparenting.data.model.ParentGuide
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class ParentViewModel(app: Application) : AndroidViewModel(app) {

    private val loader = ParentGuideLoader(app)

    // ── Raw data ──────────────────────────────────────────────────────────────
    private var allGuides: List<ParentGuide> = emptyList()

    // ── UI states ─────────────────────────────────────────────────────────────
    private val _isLoading  = MutableStateFlow(false)
    private val _error      = MutableStateFlow<String?>(null)
    private val _results    = MutableStateFlow<List<ParentGuide>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _selectedDomain = MutableStateFlow<String?>(null)
    private val _selectedAgeGroup = MutableStateFlow<String?>(null)
    private val _selectedGuide  = MutableStateFlow<ParentGuide?>(null)

    val isLoading:       StateFlow<Boolean>          = _isLoading.asStateFlow()
    val error:           StateFlow<String?>          = _error.asStateFlow()
    val results:         StateFlow<List<ParentGuide>> = _results.asStateFlow()
    val searchQuery:     StateFlow<String>           = _searchQuery.asStateFlow()
    val selectedDomain:  StateFlow<String?>          = _selectedDomain.asStateFlow()
    val selectedAgeGroup:StateFlow<String?>          = _selectedAgeGroup.asStateFlow()
    val selectedGuide:   StateFlow<ParentGuide?>     = _selectedGuide.asStateFlow()

    // Derived
    val domains: StateFlow<List<String>> get() = _domainsFlow
    val ageGroups: List<String> = listOf("0–2 Years", "2–5 Years", "5–12 Years")

    private val _domainsFlow = MutableStateFlow<List<String>>(emptyList())

    init {
        viewModelScope.launch { load() }

        // Auto-search with 300ms debounce when query or filters change
        viewModelScope.launch {
            combine(
                _searchQuery.debounce(300),
                _selectedDomain,
                _selectedAgeGroup
            ) { query, domain, ageGroup ->
                Triple(query, domain, ageGroup)
            }.collectLatest { (query, domain, ageGroup) ->
                applyFilters(query, domain, ageGroup)
            }
        }
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    private suspend fun load() {
        _isLoading.value = true
        _error.value     = null
        try {
            allGuides = loader.loadAll()
            _domainsFlow.value = allGuides
                .map { it.domain.trim().lowercase().replaceFirstChar { c -> c.uppercase() } }
                .distinct()
                .sorted()
            _results.value = allGuides
        } catch (e: Exception) {
            _error.value = "Could not load parent guides: ${e.localizedMessage}"
        } finally {
            _isLoading.value = false
        }
    }

    // ── Search / filter ───────────────────────────────────────────────────────

    fun onSearchQuery(query: String) { _searchQuery.value = query }

    fun onDomainSelected(domain: String?) { _selectedDomain.value = domain }

    fun onAgeGroupSelected(ageGroup: String?) { _selectedAgeGroup.value = ageGroup }

    fun clearFilters() {
        _searchQuery.value     = ""
        _selectedDomain.value  = null
        _selectedAgeGroup.value = null
    }

    fun openGuide(guide: ParentGuide)  { _selectedGuide.value = guide }
    fun closeGuide()                    { _selectedGuide.value = null }

    private fun applyFilters(query: String, domain: String?, ageGroup: String?) {
        var list = allGuides

        if (ageGroup != null) {
            list = list.filter { it.ageGroupLabel == ageGroup }
        }

        if (domain != null) {
            list = list.filter {
                it.domain.trim().lowercase().replaceFirstChar { c -> c.uppercase() } == domain
            }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter { g ->
                g.skillName.lowercase().contains(q)        ||
                g.domain.lowercase().contains(q)           ||
                g.whyItMatters.lowercase().contains(q)     ||
                g.howToTeach.lowercase().contains(q)       ||
                g.tip.lowercase().contains(q)              ||
                g.learningGoal.lowercase().contains(q)
            }
        }

        _results.value = list
    }
}
