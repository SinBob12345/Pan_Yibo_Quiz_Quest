package com.yibopan.quizquest.viewmodel

import androidx.lifecycle.ViewModel
import com.yibopan.quizquest.data.rules.LevelPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for the Result screen (Lecture 4).
 *
 * Receives the finished session ID, category ID and completed level.
 */
class ResultViewModel(
    sessionId: Long,
    categoryId: Int = 9,
    level: Int = 1,
) : ViewModel() {

    // Lecture 7: private MutableStateFlow + public read-only StateFlow.
    private val _uiState = MutableStateFlow(
        ResultUIState(
            sessionId = sessionId,
            categoryId = categoryId,
            level = level,
            isNextLevelUnlocked = level < LevelPolicy.LEVELS_PER_WORLD,
        ),
    )
    val uiState: StateFlow<ResultUIState> get() = _uiState
}
