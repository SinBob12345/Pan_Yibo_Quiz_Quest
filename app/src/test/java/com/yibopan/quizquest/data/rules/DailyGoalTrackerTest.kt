package com.yibopan.quizquest.data.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/** Unit tests for [DailyGoalTracker] stopping cues and weekly active day tracking. */
class DailyGoalTrackerTest {

    private val timeZone = TimeZone.getTimeZone("UTC")

    @Test
    fun `isGoalReached returns true when completed levels meet or exceed goal`() {
        assertTrue(DailyGoalTracker.isGoalReached(3, 3))
        assertTrue(DailyGoalTracker.isGoalReached(5, 3))
    }

    @Test
    fun `isGoalReached returns false when completed levels are below goal`() {
        assertFalse(DailyGoalTracker.isGoalReached(2, 3))
        assertFalse(DailyGoalTracker.isGoalReached(0, 1))
    }

    @Test
    fun `isGoalReached returns false when goal is zero or negative`() {
        assertFalse(DailyGoalTracker.isGoalReached(5, 0))
        assertFalse(DailyGoalTracker.isGoalReached(5, -1))
    }

    @Test
    fun `startOfDayMillis returns midnight timestamp for given timezone`() {
        val calendar = Calendar.getInstance(timeZone).apply {
            set(2025, Calendar.MAY, 15, 14, 30, 45)
            set(Calendar.MILLISECOND, 500)
        }
        val nowMillis = calendar.timeInMillis

        val startOfDay = DailyGoalTracker.startOfDayMillis(nowMillis, timeZone)

        val midnightCalendar = Calendar.getInstance(timeZone).apply {
            timeInMillis = startOfDay
        }
        assertEquals(2025, midnightCalendar.get(Calendar.YEAR))
        assertEquals(Calendar.MAY, midnightCalendar.get(Calendar.MONTH))
        assertEquals(15, midnightCalendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, midnightCalendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, midnightCalendar.get(Calendar.MINUTE))
        assertEquals(0, midnightCalendar.get(Calendar.SECOND))
        assertEquals(0, midnightCalendar.get(Calendar.MILLISECOND))
    }

    @Test
    fun `startOfDayMillis handles daylight saving time transitions`() {
        val dstZone = TimeZone.getTimeZone("America/New_York")
        val calendar = Calendar.getInstance(dstZone).apply {
            set(2025, Calendar.MARCH, 9, 10, 0, 0)
        }
        val nowMillis = calendar.timeInMillis

        val midnight = DailyGoalTracker.startOfDayMillis(nowMillis, dstZone)

        val midnightCal = Calendar.getInstance(dstZone).apply {
            timeInMillis = midnight
        }
        assertEquals(0, midnightCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(9, midnightCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `activeDaysThisWeek counts distinct learning days in Monday to Sunday week`() {
        val monday = Calendar.getInstance(timeZone).apply { set(2025, Calendar.MAY, 12, 10, 0) }.timeInMillis
        val tuesday = Calendar.getInstance(timeZone).apply { set(2025, Calendar.MAY, 13, 14, 0) }.timeInMillis
        val tuesdayLater = Calendar.getInstance(timeZone).apply { set(2025, Calendar.MAY, 13, 18, 0) }.timeInMillis
        val friday = Calendar.getInstance(timeZone).apply { set(2025, Calendar.MAY, 16, 9, 0) }.timeInMillis
        val wednesdayNow = Calendar.getInstance(timeZone).apply { set(2025, Calendar.MAY, 14, 12, 0) }.timeInMillis

        val activeCount = DailyGoalTracker.activeDaysThisWeek(
            sessionStartTimes = listOf(monday, tuesday, tuesdayLater, friday),
            nowMillis = wednesdayNow,
            timeZone = timeZone,
        )

        assertEquals(3, activeCount)
    }

    @Test
    fun `activeDaysThisWeek excludes sessions from outside the current week`() {
        val lastWeekSunday = Calendar.getInstance(timeZone).apply { set(2025, Calendar.MAY, 11, 23, 59) }.timeInMillis
        val thisMonday = Calendar.getInstance(timeZone).apply { set(2025, Calendar.MAY, 12, 10, 0) }.timeInMillis
        val nextWeekMonday = Calendar.getInstance(timeZone).apply { set(2025, Calendar.MAY, 19, 10, 0) }.timeInMillis
        val wednesdayNow = Calendar.getInstance(timeZone).apply { set(2025, Calendar.MAY, 14, 12, 0) }.timeInMillis

        val activeCount = DailyGoalTracker.activeDaysThisWeek(
            sessionStartTimes = listOf(lastWeekSunday, thisMonday, nextWeekMonday),
            nowMillis = wednesdayNow,
            timeZone = timeZone,
        )

        assertEquals(1, activeCount)
    }

    @Test
    fun `activeDaysThisWeek returns 0 for empty session list`() {
        val nowMillis = System.currentTimeMillis()
        assertEquals(0, DailyGoalTracker.activeDaysThisWeek(emptyList(), nowMillis, timeZone))
    }
}
