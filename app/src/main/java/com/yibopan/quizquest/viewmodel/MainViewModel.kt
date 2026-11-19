package com.yibopan.quizquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yibopan.quizquest.data.model.ThemeMode
import com.yibopan.quizquest.data.repository.PrivacyRepository
import com.yibopan.quizquest.data.repository.SettingsRepository
import com.yibopan.quizquest.data.rules.SystemTimeProvider
import com.yibopan.quizquest.data.rules.TimeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * App-level ViewModel (Lecture 4) for reading global theme preferences from [SettingsRepository]
 * and running startup maintenance such as purging expired answer records according to retention settings.
 */
class MainViewModel(
    private val settingsRepository: SettingsRepository,
    private val privacyRepository: PrivacyRepository,
    private val timeProvider: TimeProvider = SystemTimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUIState())
    val uiState: StateFlow<MainUIState> get() = _uiState

    init {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                _uiState.update { it.copy(themeMode = settings.themeMode) }
            }
        }
        viewModelScope.launch {
            privacyRepository.deleteExpiredAnswers(timeProvider.now())
        }
    }

    /** Resolves whether dark theme should be enabled based on current [ThemeMode] and system dark mode state. */
    fun shouldEnableDarkTheme(systemInDark: Boolean): Boolean = shouldEnableDarkTheme(_uiState.value.themeMode, systemInDark)

    /** Resolves whether dark theme should be enabled for the given [themeMode] and [systemInDark] state. */
    fun shouldEnableDarkTheme(themeMode: ThemeMode, systemInDark: Boolean): Boolean = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
}
