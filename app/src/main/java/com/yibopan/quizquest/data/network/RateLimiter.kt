package com.yibopan.quizquest.data.network

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Stage 2 — guarantees at least [minIntervalMillis] between the starts of two API calls
 * (Open Trivia DB: one request per IP every 5 seconds, otherwise response code 5).
 *
 * Implementation notes (Lecture 5 coroutines):
 *  - use a kotlinx.coroutines.sync.Mutex so concurrent callers wait their turn
 *  - wait with delay(remaining) — never Thread.sleep — so the main thread is not blocked
 *    and tests can use virtual time
 *  - [now] is injected; tests pass `{ testScheduler.currentTime }`
 */
class RateLimiter(
    private val minIntervalMillis: Long = DEFAULT_MIN_INTERVAL_MS,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val mutex = Mutex()
    private var lastCallTime: Long? = null

    suspend fun <T> throttle(block: suspend () -> T): T {
        return mutex.withLock {
            val last = lastCallTime
            val currentTime = now()
            if (last != null) {
                val elapsed = currentTime - last
                val waitTime = minIntervalMillis - elapsed
                if (waitTime > 0) {
                    delay(waitTime)
                }
            }
            lastCallTime = now()
            block()
        }
    }

    companion object {
        const val DEFAULT_MIN_INTERVAL_MS = 5_000L
    }
}
