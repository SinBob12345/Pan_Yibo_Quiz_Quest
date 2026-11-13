package com.yibopan.quizquest.data.rules

import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for [StarRating] boundary and calculation rules. */
class StarRatingTest {

    @Test
    fun `starsFor returns 0 stars when accuracy is less than 60 percent`() {
        assertEquals(0, StarRating.starsFor(0, 10))
        assertEquals(0, StarRating.starsFor(5, 10))
        assertEquals(0, StarRating.starsFor(2, 5))
        assertEquals(0, StarRating.starsFor(8, 15))
    }

    @Test
    fun `starsFor returns 1 star when accuracy is between 60 percent and 79 percent`() {
        assertEquals(1, StarRating.starsFor(6, 10))
        assertEquals(1, StarRating.starsFor(7, 10))
        assertEquals(1, StarRating.starsFor(3, 5))
        assertEquals(1, StarRating.starsFor(9, 15))
        assertEquals(1, StarRating.starsFor(11, 15))
    }

    @Test
    fun `starsFor returns 2 stars when accuracy is between 80 percent and 99 percent`() {
        assertEquals(2, StarRating.starsFor(8, 10))
        assertEquals(2, StarRating.starsFor(9, 10))
        assertEquals(2, StarRating.starsFor(4, 5))
        assertEquals(2, StarRating.starsFor(12, 15))
        assertEquals(2, StarRating.starsFor(14, 15))
    }

    @Test
    fun `starsFor returns 3 stars when accuracy is 100 percent`() {
        assertEquals(3, StarRating.starsFor(10, 10))
        assertEquals(3, StarRating.starsFor(5, 5))
        assertEquals(3, StarRating.starsFor(15, 15))
    }

    @Test
    fun `starsFor returns 0 stars when total is 0`() {
        assertEquals(0, StarRating.starsFor(0, 0))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `starsFor throws IllegalArgumentException when correct is negative`() {
        StarRating.starsFor(-1, 10)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `starsFor throws IllegalArgumentException when total is negative`() {
        StarRating.starsFor(0, -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `starsFor throws IllegalArgumentException when correct is greater than total`() {
        StarRating.starsFor(11, 10)
    }
}
