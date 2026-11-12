package com.yibopan.quizquest.data.model

/**
 * Stored progress of one played level. Levels never played have no row.
 * Whether a level is unlocked is NOT stored — it is worked out with LevelPolicy.
 */
data class LevelProgress(
    val categoryId: Int,
    val level: Int,
    val stars: Int,
    val bestScore: Int,
)

/** One answered question inside a quiz session. [chosenAnswer] is null when the timer ran out. */
data class AnswerRecord(
    val questionText: String,
    val chosenAnswer: String?,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val timeTakenMs: Long,
)

/** Summary of a finished quiz session (one level attempt). */
data class QuizResult(
    val sessionId: Long,
    val categoryId: Int,
    val level: Int,
    val correctCount: Int,
    val totalCount: Int,
    val stars: Int,
    val startedAt: Long,
    val durationMs: Long,
)
