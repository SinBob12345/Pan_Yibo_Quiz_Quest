package com.yibopan.quizquest.viewmodel

import androidx.annotation.StringRes
import com.yibopan.quizquest.data.model.ReviewCard

/** Everything the Review (flashcards) screen shows. */
data class ReviewUIState(
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val dueCards: List<ReviewCard> = emptyList(),
    /** False = question side, true = answer side of the flashcard. */
    val isAnswerRevealed: Boolean = false,
    val reviewedThisSession: Int = 0,
    @param:StringRes val errorMessageRes: Int? = null,
) {
    val currentCard: ReviewCard? get() = dueCards.firstOrNull()
}
