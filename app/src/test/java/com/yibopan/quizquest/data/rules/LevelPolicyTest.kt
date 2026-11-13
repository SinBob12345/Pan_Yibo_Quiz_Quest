package com.yibopan.quizquest.data.rules

import com.yibopan.quizquest.data.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for [LevelPolicy] difficulty mapping and unlocking rules. */
class LevelPolicyTest {

    @Test
    fun `difficultyFor returns EASY for levels 1 to 3`() {
        assertEquals(Difficulty.EASY, LevelPolicy.difficultyFor(1))
        assertEquals(Difficulty.EASY, LevelPolicy.difficultyFor(2))
        assertEquals(Difficulty.EASY, LevelPolicy.difficultyFor(3))
    }

    @Test
    fun `difficultyFor returns MEDIUM for levels 4 to 6`() {
        assertEquals(Difficulty.MEDIUM, LevelPolicy.difficultyFor(4))
        assertEquals(Difficulty.MEDIUM, LevelPolicy.difficultyFor(5))
        assertEquals(Difficulty.MEDIUM, LevelPolicy.difficultyFor(6))
    }

    @Test
    fun `difficultyFor returns HARD for levels 7 to 9`() {
        assertEquals(Difficulty.HARD, LevelPolicy.difficultyFor(7))
        assertEquals(Difficulty.HARD, LevelPolicy.difficultyFor(8))
        assertEquals(Difficulty.HARD, LevelPolicy.difficultyFor(9))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `difficultyFor throws IllegalArgumentException for level 0`() {
        LevelPolicy.difficultyFor(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `difficultyFor throws IllegalArgumentException for level 10`() {
        LevelPolicy.difficultyFor(10)
    }

    @Test
    fun `isUnlocked returns true for level 1 regardless of previous stars`() {
        assertTrue(LevelPolicy.isUnlocked(1, null))
        assertTrue(LevelPolicy.isUnlocked(1, 0))
    }

    @Test
    fun `isUnlocked returns true for level greater than 1 only if previous level has at least 1 star`() {
        assertFalse(LevelPolicy.isUnlocked(2, null))
        assertFalse(LevelPolicy.isUnlocked(2, 0))
        assertTrue(LevelPolicy.isUnlocked(2, 1))
        assertTrue(LevelPolicy.isUnlocked(2, 2))
        assertTrue(LevelPolicy.isUnlocked(2, 3))

        assertTrue(LevelPolicy.isUnlocked(9, 1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `isUnlocked throws IllegalArgumentException for level 0`() {
        LevelPolicy.isUnlocked(0, 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `isUnlocked throws IllegalArgumentException for level 10`() {
        LevelPolicy.isUnlocked(10, 1)
    }
}
