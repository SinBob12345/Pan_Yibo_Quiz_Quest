package com.yibopan.quizquest.data.network

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for [RateLimiter] timing rules using coroutine virtual time. */
@OptIn(ExperimentalCoroutinesApi::class)
class RateLimiterTest {

    @Test
    fun `first call executes immediately without delay`() = runTest {
        val rateLimiter = RateLimiter(minIntervalMillis = 5000L) { testScheduler.currentTime }
        var executedTime = -1L

        rateLimiter.throttle {
            executedTime = testScheduler.currentTime
        }

        assertEquals(0L, executedTime)
        assertEquals(0L, testScheduler.currentTime)
    }

    @Test
    fun `second rapid call is delayed by minimum interval`() = runTest {
        val rateLimiter = RateLimiter(minIntervalMillis = 5000L) { testScheduler.currentTime }
        var call1Time = -1L
        var call2Time = -1L

        rateLimiter.throttle {
            call1Time = testScheduler.currentTime
        }
        advanceTimeBy(1000L)
        rateLimiter.throttle {
            call2Time = testScheduler.currentTime
        }

        assertEquals(0L, call1Time)
        assertEquals(5000L, call2Time)
        assertEquals(5000L, testScheduler.currentTime)
    }

    @Test
    fun `call after minimum interval executes without delay`() = runTest {
        val rateLimiter = RateLimiter(minIntervalMillis = 5000L) { testScheduler.currentTime }
        var call1Time = -1L
        var call2Time = -1L

        rateLimiter.throttle {
            call1Time = testScheduler.currentTime
        }
        advanceTimeBy(6000L)
        rateLimiter.throttle {
            call2Time = testScheduler.currentTime
        }

        assertEquals(0L, call1Time)
        assertEquals(6000L, call2Time)
        assertEquals(6000L, testScheduler.currentTime)
    }

    @Test
    fun `concurrent callers are throttled sequentially`() = runTest {
        val rateLimiter = RateLimiter(minIntervalMillis = 5000L) { testScheduler.currentTime }
        val executionTimes = mutableListOf<Long>()

        val job1 = launch {
            rateLimiter.throttle {
                executionTimes.add(testScheduler.currentTime)
            }
        }
        val job2 = launch {
            rateLimiter.throttle {
                executionTimes.add(testScheduler.currentTime)
            }
        }
        val job3 = launch {
            rateLimiter.throttle {
                executionTimes.add(testScheduler.currentTime)
            }
        }

        joinAll(job1, job2, job3)

        assertEquals(listOf(0L, 5000L, 10000L), executionTimes)
    }

    @Test
    fun `custom minimum interval is respected`() = runTest {
        val rateLimiter = RateLimiter(minIntervalMillis = 2000L) { testScheduler.currentTime }
        var call1Time = -1L
        var call2Time = -1L

        rateLimiter.throttle {
            call1Time = testScheduler.currentTime
        }
        rateLimiter.throttle {
            call2Time = testScheduler.currentTime
        }

        assertEquals(0L, call1Time)
        assertEquals(2000L, call2Time)
    }
}
