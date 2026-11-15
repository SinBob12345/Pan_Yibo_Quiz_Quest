package com.yibopan.quizquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yibopan.quizquest.data.repository.ProgressRepository
import com.yibopan.quizquest.data.repository.SettingsRepository
import com.yibopan.quizquest.data.rules.DailyGoalTracker
import com.yibopan.quizquest.data.rules.LevelPolicy
import com.yibopan.quizquest.data.rules.StarRating
import com.yibopan.quizquest.data.rules.SystemTimeProvider
import com.yibopan.quizquest.data.rules.TimeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.TimeZone

/**
 * ViewModel for the Result screen (Lecture 4 / Lecture 5).
 *
 * Receives the finished session ID, category ID, and completed level.
 * Collects stored session results and daily goal progress to detect the
 * "done for today" stopping cue (A2 commitment #6).
 */
class ResultViewModel(
    private val sessionId: Long,
    private val categoryId: Int = 9,
    private val level: Int = 1,
    private val progressRepository: ProgressRepository,
    private val settingsRepository: SettingsRepository,
    private val timeProvider: TimeProvider = SystemTimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ResultUIState(
            sessionId = sessionId,
            categoryId = categoryId,
            level = level,
            isNextLevelUnlocked = level < LevelPolicy.LEVELS_PER_WORLD,
        ),
    )
    val uiState: StateFlow<ResultUIState> get() = _uiState

    init {
        loadResult()
    }

    private fun loadResult() {
        viewModelScope.launch {
            val now = timeProvider.now()
            val startOfDay = DailyGoalTracker.startOfDayMillis(now, TimeZone.getDefault())

            combine(
                progressRepository.getQuizResult(sessionId),
                progressRepository.getLevelsCompletedSince(startOfDay),
                settingsRepository.getSettings(),
            ) { result, levelsCompletedToday, settings ->
                val currentCategory = result?.categoryId ?: categoryId
                val currentLevel = result?.level ?: level
                val correctCount = result?.correctCount ?: 0
                val totalCount = result?.totalCount ?: 0
                val stars = result?.stars ?: 0

                val isNextUnlocked = stars >= StarRating.PASS_STARS && currentLevel < LevelPolicy.LEVELS_PER_WORLD
                val goalReached = DailyGoalTracker.isGoalReached(levelsCompletedToday, settings.dailyGoalLevels)

                ResultUIState(
                    sessionId = sessionId,
                    categoryId = currentCategory,
                    level = currentLevel,
                    correctCount = correctCount,
                    totalCount = totalCount,
                    stars = stars,
                    isNextLevelUnlocked = isNextUnlocked,
                    isDailyGoalReached = goalReached,
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}
