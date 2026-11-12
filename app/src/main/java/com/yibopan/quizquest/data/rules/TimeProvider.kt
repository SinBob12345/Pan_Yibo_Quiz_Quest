package com.yibopan.quizquest.data.rules

/**
 * Gives the current time in epoch milliseconds. It is injected with Koin so tests can use a
 * fixed time instead of the real clock (Lecture 4: dependency injection improves testability).
 * `fun interface` lets tests write `TimeProvider { 1_000L }` (Lecture 1: functional types).
 */
fun interface TimeProvider {
    fun now(): Long
}

/** The real clock, used by the app. */
object SystemTimeProvider : TimeProvider {
    override fun now(): Long = System.currentTimeMillis()
}
