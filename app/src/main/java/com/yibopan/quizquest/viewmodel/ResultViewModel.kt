package com.yibopan.quizquest.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Stage 1: placeholder values (world 9, level 1, next level unlocked).
 *
 * Stage 5A: inject ProgressRepository, SettingsRepository and TimeProvider; collect
 * getQuizResult(sessionId) into ResultUIState.
 */
class ResultViewModel(sessionId: Long) : ViewModel() {

    // Lecture 7: private MutableStateFlow + public read-only StateFlow.
    private val _uiState = MutableStateFlow(
        ResultUIState(
            sessionId = sessionId,
            categoryId = PLACEHOLDER_CATEGORY_ID,
            level = 1,
            isNextLevelUnlocked = true,
        ),
    )
    val uiState: StateFlow<ResultUIState> get() = _uiState

    private companion object {
        const val PLACEHOLDER_CATEGORY_ID = 9
    }
}
