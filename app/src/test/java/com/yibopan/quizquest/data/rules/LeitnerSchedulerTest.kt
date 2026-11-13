package com.yibopan.quizquest.data.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for [LeitnerScheduler] spaced repetition rules. */
class LeitnerSchedulerTest {

    private val now = 1_000_000_000L
    private val millisPerDay = 86_400_000L

    @Test
    fun `nextBox moves up one box when correct and caps at MAX_BOX`() {
        assertEquals(2, LeitnerScheduler.nextBox(1, correct = true))
        assertEquals(3, LeitnerScheduler.nextBox(2, correct = true))
        assertEquals(4, LeitnerScheduler.nextBox(3, correct = true))
        assertEquals(5, LeitnerScheduler.nextBox(4, correct = true))
        assertEquals(5, LeitnerScheduler.nextBox(5, correct = true))
    }

    @Test
    fun `nextBox returns to box 1 when wrong`() {
        assertEquals(1, LeitnerScheduler.nextBox(1, correct = false))
        assertEquals(1, LeitnerScheduler.nextBox(2, correct = false))
        assertEquals(1, LeitnerScheduler.nextBox(3, correct = false))
        assertEquals(1, LeitnerScheduler.nextBox(4, correct = false))
        assertEquals(1, LeitnerScheduler.nextBox(5, correct = false))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `nextBox throws IllegalArgumentException when box is 0`() {
        LeitnerScheduler.nextBox(0, correct = true)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `nextBox throws IllegalArgumentException when box is 6`() {
        LeitnerScheduler.nextBox(6, correct = true)
    }

    @Test
    fun `nextReviewAt adds correct interval for each box`() {
        assertEquals(now + (1 * millisPerDay), LeitnerScheduler.nextReviewAt(1, now))
        assertEquals(now + (2 * millisPerDay), LeitnerScheduler.nextReviewAt(2, now))
        assertEquals(now + (4 * millisPerDay), LeitnerScheduler.nextReviewAt(3, now))
        assertEquals(now + (7 * millisPerDay), LeitnerScheduler.nextReviewAt(4, now))
        assertEquals(now + (14 * millisPerDay), LeitnerScheduler.nextReviewAt(5, now))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `nextReviewAt throws IllegalArgumentException when box is 0`() {
        LeitnerScheduler.nextReviewAt(0, now)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `nextReviewAt throws IllegalArgumentException when box is 6`() {
        LeitnerScheduler.nextReviewAt(6, now)
    }

    @Test
    fun `isDue returns true when nextReviewAt is equal or in the past`() {
        assertTrue(LeitnerScheduler.isDue(now - 1000L, now))
        assertTrue(LeitnerScheduler.isDue(now, now))
    }

    @Test
    fun `isDue returns false when nextReviewAt is in the future`() {
        assertFalse(LeitnerScheduler.isDue(now + 1000L, now))
    }
}
