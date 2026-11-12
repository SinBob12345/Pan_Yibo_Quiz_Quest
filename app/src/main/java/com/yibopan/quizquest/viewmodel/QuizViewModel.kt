package com.yibopan.quizquest.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Stage 1: placeholder.
 *
 * Stage 5A: inject TriviaRepository, ProgressRepository, ReviewRepository, SettingsRepository,
 * QuizEngine and TimeProvider. In init { } load the questions (LevelPolicy.difficultyFor(level),
 * settings.questionsPerLevel) in viewModelScope.launch, handle NetworkResult.Success / Error
 * (Lecture 5), keep a QuizState and map it to QuizUIState. When the quiz is finished, save it and
 * set finishedSessionId.
 */
class QuizViewModel(
    categoryId: Int,
    level: Int,
) : ViewModel() {

    // Lecture 7: private MutableStateFlow + public read-only StateFlow.
    private val _uiState = MutableStateFlow(QuizUIState(categoryId = categoryId, level = level))
    val uiState: StateFlow<QuizUIState> get() = _uiState
}
