package com.yibopan.quizquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yibopan.quizquest.data.repository.StatsRepository
import com.yibopan.quizquest.util.AppLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
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

    private companion object {
        private const val TAG = "StatsViewModel"
    }

    private val _uiState = MutableStateFlow(StatsUIState(isLoading = true))
    val uiState: StateFlow<StatsUIState> get() = _uiState

    init {
        viewModelScope.launch {
            statsRepository.getStatsSummary()
                .catch { e ->
                    AppLog.e(TAG, "Error collecting stats summary", e)
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load statistics.") }
                }
                .collect { summary ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            summary = summary,
                            error = null,
                        )
                    }
                }
        }
    }
}
