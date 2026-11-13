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

    private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L

    fun nextBox(currentBox: Int, correct: Boolean): Int {
        require(currentBox in MIN_BOX..MAX_BOX) {
            "Box must be between $MIN_BOX and $MAX_BOX, got $currentBox"
        }
        return if (correct) {
            (currentBox + 1).coerceAtMost(MAX_BOX)
        } else {
            MIN_BOX
        }
    }

    fun nextReviewAt(box: Int, nowMillis: Long): Long {
        require(box in MIN_BOX..MAX_BOX) {
            "Box must be between $MIN_BOX and $MAX_BOX, got $box"
        }
        val days = INTERVAL_DAYS[box - 1]
        return nowMillis + (days * MILLIS_PER_DAY)
    }

    fun isDue(nextReviewAt: Long, nowMillis: Long): Boolean {
        return nextReviewAt <= nowMillis
    }
}
