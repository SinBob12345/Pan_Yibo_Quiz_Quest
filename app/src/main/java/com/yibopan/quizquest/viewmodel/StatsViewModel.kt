package com.yibopan.quizquest.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Stage 1: placeholder (empty summary).
 *
 * Stage 6B: inject StatsRepository; collect getStatsSummary().asResult() like Lecture 7 getPets().
 */
class StatsViewModel : ViewModel() {

    // Lecture 7: private MutableStateFlow + public read-only StateFlow.
    private val _uiState = MutableStateFlow(StatsUIState())
    val uiState: StateFlow<StatsUIState> get() = _uiState
}
