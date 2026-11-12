package com.yibopan.quizquest.data.network

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

    suspend fun <T> throttle(block: suspend () -> T): T = TODO("Stage 2")

    companion object {
        const val DEFAULT_MIN_INTERVAL_MS = 5_000L
    }
}
