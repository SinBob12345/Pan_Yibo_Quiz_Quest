package com.yibopan.quizquest.viewmodel

import com.yibopan.quizquest.data.model.ReviewCard

/** Everything the Review (flashcards) screen shows. */
data class ReviewUIState(
    val isLoading: Boolean = false,
    val dueCards: List<ReviewCard> = emptyList(),
    val currentIndex: Int = 0,
    /** False = question side, true = answer side of the flashcard. */
    val isAnswerRevealed: Boolean = false,
) {
    val currentCard: ReviewCard? get() = dueCards.getOrNull(currentIndex)
}
