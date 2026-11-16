package com.yibopan.quizquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yibopan.quizquest.data.model.ThemeMode
import com.yibopan.quizquest.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * App-level ViewModel (Lecture 4) for reading global theme preferences from [SettingsRepository].
 */
class MainViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUIState())
    val uiState: StateFlow<MainUIState> get() = _uiState

    init {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                _uiState.update { it.copy(themeMode = settings.themeMode) }
            }
        }
    }

    /** Resolves whether dark theme should be enabled based on current [ThemeMode] and system dark mode state. */
    fun shouldEnableDarkTheme(systemInDark: Boolean): Boolean = when (_uiState.value.themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
}
