package com.yibopan.quizquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yibopan.quizquest.data.repository.StatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the User Statistics screen (Lecture 4 / Lecture 7).
 *
 * Collects read-only learning statistics from [StatsRepository].
 */
class StatsViewModel(
    private val statsRepository: StatsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUIState(isLoading = true))
    val uiState: StateFlow<StatsUIState> get() = _uiState

    init {
        viewModelScope.launch {
            statsRepository.getStatsSummary().collect { summary ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        summary = summary,
                    )
                }
            }
        }
    }
}
