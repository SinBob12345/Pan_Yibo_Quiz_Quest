package com.yibopan.quizquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yibopan.quizquest.data.repository.ProgressRepository
import com.yibopan.quizquest.data.repository.ReviewRepository
import com.yibopan.quizquest.data.repository.SettingsRepository
import com.yibopan.quizquest.data.repository.StatsRepository
import com.yibopan.quizquest.data.repository.TriviaRepository
import com.yibopan.quizquest.data.rules.DailyGoalTracker
import com.yibopan.quizquest.data.rules.SystemTimeProvider
import com.yibopan.quizquest.data.rules.TimeProvider
import com.yibopan.quizquest.util.AppLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.TimeZone

/**
 * ViewModel for the Home landing screen (Lecture 4 / Lecture 5).
 *
 * Collects categories, daily goal progress, review card counts, and statistics
 * to show today's summary and gentle stopping cues (A2 commitment #6).
 */
class HomeViewModel(
    private val triviaRepository: TriviaRepository,
    private val progressRepository: ProgressRepository,
    private val reviewRepository: ReviewRepository,
    private val statsRepository: StatsRepository,
    private val settingsRepository: SettingsRepository,
    private val timeProvider: TimeProvider = SystemTimeProvider,
) : ViewModel() {

    private companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _uiState = MutableStateFlow(HomeUIState(isLoading = true))
    val uiState: StateFlow<HomeUIState> get() = _uiState

    init {
        observeHomeData()
    }

    private fun observeHomeData() {
        viewModelScope.launch {
            val now = timeProvider.now()
            val startOfDay = DailyGoalTracker.startOfDayMillis(now, TimeZone.getDefault())

            combine(
                triviaRepository.getCategories(),
                progressRepository.getLevelsCompletedSince(startOfDay),
                reviewRepository.getDueCount(now),
                statsRepository.getStatsSummary(),
                settingsRepository.getSettings(),
            ) { worlds, levelsCompleted, dueCount, stats, settings ->
                val questionsToday = stats.last7Days.lastOrNull()?.questionsAnswered ?: 0
                val goal = settings.dailyGoalLevels
                val isGoalReached = DailyGoalTracker.isGoalReached(levelsCompleted, goal)

                HomeUIState(
                    isLoading = false,
                    worldsUnavailable = worlds.isEmpty(),
                    isRetrying = _uiState.value.isRetrying,
                    worlds = worlds,
                    questionsAnsweredToday = questionsToday,
                    levelsCompletedToday = levelsCompleted,
                    dailyGoal = goal,
                    isDailyGoalReached = isGoalReached,
                    dueReviewCount = dueCount,
                    activeDaysThisWeek = stats.activeDaysThisWeek,
                )
            }.catch { e ->
                AppLog.e(TAG, "Error observing home data", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        worldsUnavailable = true,
                    )
                }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun retry() {
        if (_uiState.value.isRetrying) return
        _uiState.update { it.copy(isRetrying = true) }

        viewModelScope.launch {
            try {
                triviaRepository.fetchRemoteCategories()
            } catch (e: Exception) {
                AppLog.e(TAG, "Failed to fetch remote categories on retry", e)
            } finally {
                _uiState.update { it.copy(isRetrying = false) }
            }
        }
    }
}
