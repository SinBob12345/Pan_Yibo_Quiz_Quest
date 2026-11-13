package com.yibopan.quizquest.data.rules

import com.yibopan.quizquest.data.model.RetentionPeriod

/** Stage 3 — data retention (A2 slide 25: "a DAO query deletes records after N days"). */
object RetentionPolicy {

    private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L

    /**
     * Records with a timestamp strictly before the returned value must be deleted.
     * Returns null for [RetentionPeriod.FOREVER] (nothing is deleted).
     */
    fun cutoffMillis(period: RetentionPeriod, nowMillis: Long): Long? {
        val days = period.days ?: return null
        return nowMillis - (days * MILLIS_PER_DAY)
    }
}
