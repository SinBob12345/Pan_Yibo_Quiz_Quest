package com.yibopan.quizquest.data.rules

/**
 * Stage 3 — converts a level score into 0..3 stars.
 *
 * Mastery-based and fully predictable (no random rewards — A2 commitment #6):
 *  - accuracy  < 60 %  -> 0 stars (level not passed)
 *  - accuracy >= 60 %  -> 1 star
 *  - accuracy >= 80 %  -> 2 stars
 *  - accuracy == 100 % -> 3 stars
 *
 * `total == 0` returns 0. Negative values or `correct > total` throw IllegalArgumentException.
 */
object StarRating {

    const val MAX_STARS = 3

    /** Minimum stars needed to pass a level and unlock the next one. */
    const val PASS_STARS = 1

    fun starsFor(correct: Int, total: Int): Int {
        require(correct >= 0 && total >= 0 && correct <= total) {
            "Invalid score: correct=$correct, total=$total"
        }
        if (total == 0) return 0
        val percentage = (correct.toDouble() / total.toDouble()) * 100.0
        return when {
            percentage >= 100.0 -> 3
            percentage >= 80.0 -> 2
            percentage >= 60.0 -> 1
            else -> 0
        }
    }
}
