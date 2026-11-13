package com.yibopan.quizquest.data.rules

import java.util.Calendar
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
    fun isGoalReached(levelsCompletedToday: Int, dailyGoal: Int): Boolean {
        if (dailyGoal <= 0) return false
        return levelsCompletedToday >= dailyGoal
    }

    /** Epoch millis of local midnight for the day containing [nowMillis] in [timeZone]. */
    fun startOfDayMillis(nowMillis: Long, timeZone: TimeZone): Long {
        val calendar = Calendar.getInstance(timeZone).apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    /** Number of distinct days in the current Monday–Sunday week with at least one session. */
    fun activeDaysThisWeek(sessionStartTimes: List<Long>, nowMillis: Long, timeZone: TimeZone): Int {
        if (sessionStartTimes.isEmpty()) return 0

        val cal = Calendar.getInstance(timeZone).apply {
            timeInMillis = nowMillis
        }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = (dayOfWeek - Calendar.MONDAY + 7) % 7

        cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfWeekMillis = cal.timeInMillis

        cal.add(Calendar.DAY_OF_MONTH, 7)
        val endOfWeekMillis = cal.timeInMillis

        val activeDays = mutableSetOf<Long>()
        for (time in sessionStartTimes) {
            if (time in startOfWeekMillis until endOfWeekMillis) {
                activeDays.add(startOfDayMillis(time, timeZone))
            }
        }
        return activeDays.size
    }
}
