package com.example.babyparenting.ui.viewmodel

import android.content.Context
import com.example.babyparenting.data.local.UserManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.babyparenting.data.model.*
import com.example.babyparenting.data.repository.MillionaireRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

sealed class StrategiesUiState {
    object Loading : StrategiesUiState()
    data class Success(val strategies: List<Strategy>) : StrategiesUiState()
    data class Error(val message: String) : StrategiesUiState()
}

sealed class ActivitiesUiState {
    object Loading : ActivitiesUiState()
    data class Success(val activities: List<Activity>) : ActivitiesUiState()
    data class Error(val message: String) : ActivitiesUiState()
}

sealed class ActivityDetailUiState {
    object Loading : ActivityDetailUiState()
    data class Success(val activity: Activity) : ActivityDetailUiState()
    data class Error(val message: String) : ActivityDetailUiState()
}

sealed class DailyActivityUiState {
    object Loading : DailyActivityUiState()
    data class Success(val activity: DailyActivityResponse) : DailyActivityUiState()
    data class Error(val message: String) : DailyActivityUiState()
}

sealed class ProgressUiState {
    object Loading : ProgressUiState()
    data class Success(val progress: ProgressSummary) : ProgressUiState()
    data class Error(val message: String) : ProgressUiState()
}

sealed class CompletionUiState {
    object Idle : CompletionUiState()
    object Loading : CompletionUiState()
    data class Success(val message: String) : CompletionUiState()
    data class Error(val message: String) : CompletionUiState()
}

@HiltViewModel
class MillionaireViewModel @Inject constructor(
    private val repository: MillionaireRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _strategiesState = MutableStateFlow<StrategiesUiState>(StrategiesUiState.Loading)
    val strategiesState: StateFlow<StrategiesUiState> = _strategiesState.asStateFlow()

    private val _activitiesState = MutableStateFlow<ActivitiesUiState>(ActivitiesUiState.Loading)
    val activitiesState: StateFlow<ActivitiesUiState> = _activitiesState.asStateFlow()

    private val _activityDetailState = MutableStateFlow<ActivityDetailUiState>(ActivityDetailUiState.Loading)
    val activityDetailState: StateFlow<ActivityDetailUiState> = _activityDetailState.asStateFlow()

    private val _dailyActivityState = MutableStateFlow<DailyActivityUiState>(DailyActivityUiState.Loading)
    val dailyActivityState: StateFlow<DailyActivityUiState> = _dailyActivityState.asStateFlow()

    private val _progressState = MutableStateFlow<ProgressUiState>(ProgressUiState.Loading)
    val progressState: StateFlow<ProgressUiState> = _progressState.asStateFlow()

    private val _completionState = MutableStateFlow<CompletionUiState>(CompletionUiState.Idle)
    val completionState: StateFlow<CompletionUiState> = _completionState.asStateFlow()

    // ✅ Track completed activities
    private val _completedActivities = MutableStateFlow<Set<Int>>(emptySet())
    val completedActivities: StateFlow<Set<Int>> = _completedActivities.asStateFlow()

    private val _childAge = MutableStateFlow(24) // ✅ DEFAULT: 24 months (2 years) instead of 0
    val childAge: StateFlow<Int> = _childAge.asStateFlow()

    private val _currentStrategyId = MutableStateFlow(0)
    val currentStrategyId: StateFlow<Int> = _currentStrategyId.asStateFlow()

    // ✅ Navigation event — true = back jaao
    private val _navigationEvent = MutableStateFlow(false)
    val navigationEvent: StateFlow<Boolean> = _navigationEvent.asStateFlow()

    init {
        val userId = UserManager.getUserId(context)
        loadStrategies()
        loadProgress(userId)
        loadDailyActivity(userId, _childAge.value)
        loadCompletedActivities(userId)  // ✅ Startup pe completed IDs load karo
    }

    // ✅ Backend se completed activity IDs load karo
    fun loadCompletedActivities(userId: String) {
        viewModelScope.launch {
            try {
                val response = repository.getCompletedIds(userId)
                _completedActivities.value = response.completedIds.toSet()
                Log.d("MillionaireVM", "✅ Loaded ${response.completedIds.size} completed activities from backend")
            } catch (e: Exception) {
                Log.e("MillionaireVM", "❌ Error loading completed IDs: ${e.message}")
            }
        }
    }

    // ✅ Navigation reset
    fun resetNavigationEvent() {
        _navigationEvent.value = false
    }

    fun setChildAge(ageMonths: Int) {
        // ✅ IMPORTANT: Always keep age >= 2 years (24 months) to show strategies
        val validAge = if (ageMonths < 24) 24 else ageMonths

        _childAge.value = validAge
        val ageYears = validAge / 12.0
        Log.d("MillionaireVM", "🔄 Child age set to $validAge months (${String.format("%.1f", ageYears)} years)")
        loadStrategies()
        val userId = UserManager.getUserId(context)
        loadDailyActivity(userId, validAge)
    }

    fun loadStrategies() {
        viewModelScope.launch {
            try {
                _strategiesState.value = StrategiesUiState.Loading
                Log.d("MillionaireVM", "✅ Loading strategies from backend...")
                val userId = UserManager.getUserId(context)
                val allStrategies = repository.getStrategies(userId)

                // ✅ FIX: Proper age calculation
                // - If age < 24 months, default to 24 (2 years) — minimum age for content
                // - Otherwise use ceiling to round UP
                val rawAge = _childAge.value
                val childAgeYears = if (rawAge < 24) {
                    2  // Default to 2 years
                } else {
                    kotlin.math.ceil(rawAge / 12.0).toInt()
                }

                Log.d("MillionaireVM", "🔍 Child age: $rawAge months → $childAgeYears years")

                val filteredStrategies = allStrategies.filter { strategy ->
                    val min = strategy.age_min ?: 2  // ✅ Default minimum is 2
                    val max = strategy.age_max ?: 6  // ✅ Default maximum is 6
                    val isInRange = childAgeYears >= min && childAgeYears <= max

                    if (!isInRange) {
                        Log.d("MillionaireVM", "❌ Filtered out: ${strategy.title} (requires age $min-$max, child is $childAgeYears)")
                    }
                    isInRange
                }

                Log.d("MillionaireVM", "✅ Loaded ${filteredStrategies.size}/${allStrategies.size} strategies")

                if (filteredStrategies.isEmpty()) {
                    _strategiesState.value = StrategiesUiState.Error("No strategies found for age $childAgeYears. Please set a valid child age.")
                } else {
                    _strategiesState.value = StrategiesUiState.Success(filteredStrategies)
                }
            } catch (e: Exception) {
                Log.e("MillionaireVM", "❌ Error loading strategies: ${e.message}")
                _strategiesState.value = StrategiesUiState.Error(e.message ?: "Failed to load strategies")
            }
        }
    }

    fun loadActivitiesForStrategy(strategyId: Int) {
        viewModelScope.launch {
            try {
                _activitiesState.value = ActivitiesUiState.Loading
                _currentStrategyId.value = strategyId
                Log.d("MillionaireVM", "📋 Loading activities for strategy $strategyId...")

                val allActivities = repository.getActivitiesForStrategy(strategyId)

                // ✅ FIX: Use the same age logic
                val rawAge = _childAge.value
                val childAgeYears = if (rawAge < 24) {
                    2
                } else {
                    kotlin.math.ceil(rawAge / 12.0).toInt()
                }

                val filteredActivities = allActivities.filter { activity ->
                    val min = activity.age_min ?: 2
                    val max = activity.age_max ?: 6
                    childAgeYears in min..max
                }

                Log.d("MillionaireVM", "✅ Loaded ${filteredActivities.size}/${allActivities.size} activities")

                if (filteredActivities.isEmpty()) {
                    _activitiesState.value = ActivitiesUiState.Error("No activities found for age group")
                } else {
                    _activitiesState.value = ActivitiesUiState.Success(filteredActivities)
                }
            } catch (e: Exception) {
                Log.e("MillionaireVM", "❌ Error loading activities: ${e.message}")
                _activitiesState.value = ActivitiesUiState.Error(e.message ?: "Failed to load activities")
            }
        }
    }

    fun loadActivityDetail(activityId: Int) {
        viewModelScope.launch {
            try {
                _activityDetailState.value = ActivityDetailUiState.Loading
                Log.d("MillionaireVM", "📖 Loading activity detail $activityId...")

                val activity = repository.getActivityDetail(activityId)

                Log.d("MillionaireVM", "✅ Loaded activity: ${activity.title}")
                _activityDetailState.value = ActivityDetailUiState.Success(activity)
            } catch (e: Exception) {
                Log.e("MillionaireVM", "❌ Error loading activity detail: ${e.message}")
                _activityDetailState.value = ActivityDetailUiState.Error(e.message ?: "Failed to load activity detail")
            }
        }
    }

    fun loadDailyActivity(userId: String, childAge: Int) {
        viewModelScope.launch {
            try {
                _dailyActivityState.value = DailyActivityUiState.Loading
                Log.d("MillionaireVM", "⭐ Loading daily activity for user $userId...")

                val dailyActivity = repository.getDailyActivity(userId, childAge)

                Log.d("MillionaireVM", "✅ Loaded daily activity: ${dailyActivity.activity?.title ?: "None"}")
                _dailyActivityState.value = DailyActivityUiState.Success(dailyActivity)
            } catch (e: Exception) {
                Log.e("MillionaireVM", "❌ Error loading daily activity: ${e.message}")
                _dailyActivityState.value = DailyActivityUiState.Error(e.message ?: "Failed to load daily activity")
            }
        }
    }

    fun loadProgress(userId: String) {
        viewModelScope.launch {
            try {
                _progressState.value = ProgressUiState.Loading
                Log.d("MillionaireVM", "📊 Loading progress for $userId...")

                val progress = repository.getProgress(userId)

                Log.d("MillionaireVM", "✅ Progress: ${progress.completed_activities}/${progress.total_activities}")
                _progressState.value = ProgressUiState.Success(progress)
            } catch (e: Exception) {
                Log.e("MillionaireVM", "❌ Error loading progress: ${e.message}")
                _progressState.value = ProgressUiState.Error(e.message ?: "Failed to load progress")
            }
        }
    }

    fun markActivityCompleted(
        userId: String,
        activityId: Int,
        strategyId: Int
    ) {
        viewModelScope.launch {
            try {
                _completionState.value = CompletionUiState.Loading
                Log.d("MillionaireVM", "🚀 Completing activity $activityId...")

                // ✅ Backend call
                withContext(Dispatchers.IO + NonCancellable) {
                    repository.completeActivity(userId, activityId)
                }

                Log.d("MillionaireVM", "✅ Backend success")

                // ✅ Local state update
                _completedActivities.value = _completedActivities.value + activityId
                Log.d("MillionaireVM", "🔥 Local state updated")

                // ✅ Success state
                _completionState.value = CompletionUiState.Success("Completed")

                // ✅ Reload activities for unlock
                if (strategyId > 0) {
                    loadActivitiesForStrategy(strategyId)
                }

                // ✅ Refresh progress
                loadProgress(userId)

                // ✅ 1 second baad back jaao
                delay(1000)
                _navigationEvent.value = true  // ← Screen se bahar jaao

                // ✅ Reset states
                _completionState.value = CompletionUiState.Idle

            } catch (e: CancellationException) {
                Log.e("MillionaireVM", "❌ CANCELLED → lifecycle issue")
            } catch (e: Exception) {
                Log.e("MillionaireVM", "❌ Error: ${e.message}")
                _completionState.value = CompletionUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun setSearchQuery(query: String) {
        Log.d("MillionaireVM", "Search: '$query'")
    }

    fun setAgeFilter(range: IntRange?) {
        Log.d("MillionaireVM", "Age filter: $range")
    }

    fun isActivityLocked(activities: List<Activity>, currentActivity: Activity): Boolean {
        val currentIndex = activities.indexOfFirst { it.id == currentActivity.id }
        if (currentIndex <= 0) return false

        val previousActivity = activities.getOrNull(currentIndex - 1) ?: return false
        return previousActivity.id?.let { !isActivityCompleted(it) } ?: false
    }

    fun isActivityCompleted(activityId: Int): Boolean {
        return _completedActivities.value.contains(activityId)
    }

    fun getContext(): android.content.Context = context
}