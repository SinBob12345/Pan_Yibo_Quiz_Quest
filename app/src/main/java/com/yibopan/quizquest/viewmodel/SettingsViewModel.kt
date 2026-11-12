package com.yibopan.quizquest.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Stage 1: placeholder (default settings).
 *
 * Stage 6A: inject SettingsRepository; collect getSettings(); one function per setting that calls
 * updateSettings(current.copy(...)) inside viewModelScope.launch.
 */
class SettingsViewModel : ViewModel() {

    // Lecture 7: private MutableStateFlow + public read-only StateFlow.
    private val _uiState = MutableStateFlow(SettingsUIState())
    val uiState: StateFlow<SettingsUIState> get() = _uiState
}
