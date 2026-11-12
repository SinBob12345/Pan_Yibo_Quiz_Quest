package com.yibopan.quizquest.data.rules

/**
 * Stage 3 — spaced repetition with Leitner boxes (supports memory retention).
 *
 *  - Boxes go from [MIN_BOX] to [MAX_BOX]; a new card starts in box 1.
 *  - Correct answer: move up one box (capped at MAX_BOX).
 *  - Wrong answer: back to box 1.
 *  - Next review = now + INTERVAL_DAYS[box - 1] days (1, 2, 4, 7, 14).
 *  - A card is due when nextReviewAt <= now.
 *  - Boxes outside the range throw IllegalArgumentException.
 */
object LeitnerScheduler {

    const val MIN_BOX = 1
    const val MAX_BOX = 5
    val INTERVAL_DAYS: List<Int> = listOf(1, 2, 4, 7, 14)

    fun nextBox(currentBox: Int, correct: Boolean): Int = TODO("Stage 3")

    fun nextReviewAt(box: Int, nowMillis: Long): Long = TODO("Stage 3")

    fun isDue(nextReviewAt: Long, nowMillis: Long): Boolean = TODO("Stage 3")
}
