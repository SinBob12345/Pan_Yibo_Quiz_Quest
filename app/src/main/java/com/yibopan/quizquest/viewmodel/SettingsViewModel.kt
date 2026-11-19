package com.yibopan.quizquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yibopan.quizquest.data.model.RetentionPeriod
import com.yibopan.quizquest.data.model.ThemeMode
import com.yibopan.quizquest.data.model.TimerMode
import com.yibopan.quizquest.data.model.UserSettings
import com.yibopan.quizquest.data.repository.PrivacyRepository
import com.yibopan.quizquest.data.repository.SettingsRepository
import com.yibopan.quizquest.data.rules.SystemTimeProvider
import com.yibopan.quizquest.data.rules.TimeProvider
import com.yibopan.quizquest.util.AppLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings screen (Lecture 4 / Lecture 7).
 *
 * Collects preferences from [SettingsRepository] and provides update functions
 * for questions per level, timer mode, daily goal, sound, reduce motion, theme, and history retention.
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val privacyRepository: PrivacyRepository,
    private val timeProvider: TimeProvider = SystemTimeProvider,
) : ViewModel() {

    private companion object {
        private const val TAG = "SettingsViewModel"
    }

    private val _uiState = MutableStateFlow(SettingsUIState())
    val uiState: StateFlow<SettingsUIState> get() = _uiState

    init {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
    }

    fun updateQuestionsPerLevel(count: Int) {
        updateSettings { it.copy(questionsPerLevel = count) }
    }

    fun updateTimerMode(mode: TimerMode) {
        updateSettings { it.copy(timerMode = mode) }
    }

    fun updateDailyGoal(goal: Int) {
        updateSettings { it.copy(dailyGoalLevels = goal) }
    }

    fun updateSoundEnabled(enabled: Boolean) {
        updateSettings { it.copy(soundEnabled = enabled) }
    }

    fun updateReduceMotion(reduce: Boolean) {
        updateSettings { it.copy(reduceMotion = reduce) }
    }

    fun updateThemeMode(mode: ThemeMode) {
        updateSettings { it.copy(themeMode = mode) }
    }

    fun updateAnswerRetention(period: RetentionPeriod) {
        viewModelScope.launch {
            val currentSettings = _uiState.value.settings
            settingsRepository.updateSettings(currentSettings.copy(answerRetention = period))
            try {
                privacyRepository.deleteExpiredAnswers(timeProvider.now())
            } catch (e: Exception) {
                AppLog.e(TAG, "Failed to delete expired answers after updating retention", e)
            }
        }
    }

    private fun updateSettings(transform: (UserSettings) -> UserSettings) {
        viewModelScope.launch {
            val current = _uiState.value.settings
            val updated = transform(current)
            settingsRepository.updateSettings(updated)
        }
    }
}
