package com.yibopan.quizquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yibopan.quizquest.R
import com.yibopan.quizquest.data.repository.ReviewRepository
import com.yibopan.quizquest.data.rules.SystemTimeProvider
import com.yibopan.quizquest.data.rules.TimeProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Flashcard Review screen (Lecture 4 / Lecture 7).
 *
 * Collects due cards from [ReviewRepository] and handles Leitner box promotions/demotions.
 */
class ReviewViewModel(
    private val reviewRepository: ReviewRepository,
    private val timeProvider: TimeProvider = SystemTimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUIState(isLoading = true))
    val uiState: StateFlow<ReviewUIState> get() = _uiState

    private var collectJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            val now = timeProvider.now()
            reviewRepository.getDueCards(now).collect { cards ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        dueCards = cards,
                        isAnswerRevealed = false,
                    )
                }
            }
        }
    }

    fun showAnswer() {
        if (_uiState.value.currentCard == null || _uiState.value.isUpdating) return
        _uiState.update { it.copy(isAnswerRevealed = true) }
    }

    fun onKnewIt() {
        processCardReview(correct = true)
    }

    fun onNotYet() {
        processCardReview(correct = false)
    }

    private fun processCardReview(correct: Boolean) {
        val state = _uiState.value
        val current = state.currentCard ?: return
        if (state.isUpdating) return

        _uiState.update { it.copy(isUpdating = true, errorMessageRes = null) }

        viewModelScope.launch {
            try {
                val now = timeProvider.now()
                reviewRepository.updateCard(current, correct = correct, nowMillis = now)
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        isAnswerRevealed = false,
                        reviewedThisSession = it.reviewedThisSession + 1,
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        errorMessageRes = R.string.review_error_save_card,
                    )
                }
            }
        }
    }
}
