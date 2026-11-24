package com.yibopan.quizquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yibopan.quizquest.data.model.ThemeMode
import com.yibopan.quizquest.data.model.TimerMode
import com.yibopan.quizquest.data.model.UserSettings
import com.yibopan.quizquest.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings screen (Lecture 4 / Lecture 7).
 *
 * Collects preferences from [SettingsRepository] and provides update functions
 * for questions per level, timer mode, daily goal, sound, reduce motion, and theme.
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

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

    private fun updateSettings(transform: (UserSettings) -> UserSettings) {
        viewModelScope.launch {
            val current = _uiState.value.settings
            val updated = transform(current)
            settingsRepository.updateSettings(updated)
        }
    }
}
