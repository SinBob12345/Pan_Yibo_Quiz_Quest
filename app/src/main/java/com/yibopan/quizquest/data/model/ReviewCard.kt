package com.yibopan.quizquest.data.model

/**
 * A flashcard created from a wrongly answered question.
 * [box] is the Leitner box (1..5); [nextReviewAt] is epoch millis.
 */
data class ReviewCard(
    val id: Long,
    val questionText: String,
    val correctAnswer: String,
    val box: Int,
    val nextReviewAt: Long,
    val timesWrong: Int,
)
