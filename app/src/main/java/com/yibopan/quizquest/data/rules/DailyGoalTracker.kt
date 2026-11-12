package com.yibopan.quizquest.data.rules

import java.util.TimeZone

/**
 * Stage 3 — the "done for today" stopping cue (A2 commitment #6).
 *
 * There is deliberately NO streak counter: we only count learning days in the current week
 * (Monday to Sunday), so missing a day never "breaks" anything.
 * Use java.util.Calendar (java.time needs API 26; this app supports API 24 — Lecture 1).
 */
object DailyGoalTracker {

    /** True when the learner has passed at least [dailyGoal] levels today. Goal <= 0 is never reached. */
    fun isGoalReached(levelsCompletedToday: Int, dailyGoal: Int): Boolean = TODO("Stage 3")

    /** Epoch millis of local midnight for the day containing [nowMillis] in [timeZone]. */
    fun startOfDayMillis(nowMillis: Long, timeZone: TimeZone): Long = TODO("Stage 3")

    /** Number of distinct days in the current Monday–Sunday week with at least one session. */
    fun activeDaysThisWeek(sessionStartTimes: List<Long>, nowMillis: Long, timeZone: TimeZone): Int = TODO("Stage 3")
}
