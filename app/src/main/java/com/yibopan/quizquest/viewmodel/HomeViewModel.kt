package com.yibopan.quizquest.viewmodel

import androidx.lifecycle.ViewModel
import com.yibopan.quizquest.data.model.SampleData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Stage 1: hard-coded sample worlds.
 *
 * Stage 6B: inject TriviaRepository, ProgressRepository, ReviewRepository, StatsRepository,
 * SettingsRepository and TimeProvider. In init { } launch coroutines in viewModelScope that
 * collect the repository flows (with asResult() where useful) and _uiState.update { it.copy(...) }.
 * Use DailyGoalTracker for isDailyGoalReached.
 */
class HomeViewModel : ViewModel() {

    // Lecture 7: private MutableStateFlow + public read-only StateFlow.
    private val _uiState = MutableStateFlow(HomeUIState(worlds = SampleData.worlds))
    val uiState: StateFlow<HomeUIState> get() = _uiState
}
