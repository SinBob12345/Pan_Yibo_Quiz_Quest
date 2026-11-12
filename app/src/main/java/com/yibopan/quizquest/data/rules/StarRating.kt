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

    fun starsFor(correct: Int, total: Int): Int = TODO("Stage 3: implement the star rules above")
}
