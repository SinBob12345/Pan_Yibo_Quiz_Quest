package com.yibopan.quizquest.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Stage 1: placeholder (no cards).
 *
 * Stage 6C: inject ReviewRepository + TimeProvider; collect getDueCards(now); showAnswer(),
 * onKnewIt(), onNotYet() call updateCard(...) and move to the next card.
 */
class ReviewViewModel : ViewModel() {

    // Lecture 7: private MutableStateFlow + public read-only StateFlow.
    private val _uiState = MutableStateFlow(ReviewUIState())
    val uiState: StateFlow<ReviewUIState> get() = _uiState
}
