package com.yibopan.quizquest.data.rules

import com.yibopan.quizquest.data.model.RetentionPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Unit tests for [RetentionPolicy] data retention cutoff calculations. */
class RetentionPolicyTest {

    private val nowMillis = 100_000_000_000L
    private val millisPerDay = 86_400_000L

    @Test
    fun `cutoffMillis returns 30 days ago for DAYS_30`() {
        val cutoff = RetentionPolicy.cutoffMillis(RetentionPeriod.DAYS_30, nowMillis)
        val expected = nowMillis - (30 * millisPerDay)
        assertEquals(expected, cutoff)
    }

    @Test
    fun `cutoffMillis returns 90 days ago for DAYS_90`() {
        val cutoff = RetentionPolicy.cutoffMillis(RetentionPeriod.DAYS_90, nowMillis)
        val expected = nowMillis - (90 * millisPerDay)
        assertEquals(expected, cutoff)
    }

    @Test
    fun `cutoffMillis returns null for FOREVER`() {
        val cutoff = RetentionPolicy.cutoffMillis(RetentionPeriod.FOREVER, nowMillis)
        assertNull(cutoff)
    }
}
