package com.yibopan.quizquest.data.rules

import com.yibopan.quizquest.data.model.Difficulty

/**
 * Stage 3 — rules of the level map.
 *
 *  - Each world has [LEVELS_PER_WORLD] levels.
 *  - Levels 1–3 are EASY, 4–6 MEDIUM, 7–9 HARD.
 *  - Level 1 is always unlocked; level n (n > 1) is unlocked when level n-1 has
 *    at least StarRating.PASS_STARS stars.
 *  - Levels outside 1..LEVELS_PER_WORLD throw IllegalArgumentException.
 */
object LevelPolicy {

    const val LEVELS_PER_WORLD = 9

    fun difficultyFor(level: Int): Difficulty {
        require(level in 1..LEVELS_PER_WORLD) {
            "Level must be between 1 and $LEVELS_PER_WORLD, got $level"
        }
        return when (level) {
            in 1..3 -> Difficulty.EASY
            in 4..6 -> Difficulty.MEDIUM
            else -> Difficulty.HARD
        }
    }

    /** @param previousLevelStars stars of level-1, or null if it was never played. */
    fun isUnlocked(level: Int, previousLevelStars: Int?): Boolean {
        require(level in 1..LEVELS_PER_WORLD) {
            "Level must be between 1 and $LEVELS_PER_WORLD, got $level"
        }
        if (level == 1) return true
        return previousLevelStars != null && previousLevelStars >= StarRating.PASS_STARS
    }
}
