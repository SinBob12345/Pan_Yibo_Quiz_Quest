package com.yibopan.quizquest.data.rules

import com.yibopan.quizquest.data.model.RetentionPeriod

/** Stage 3 — data retention (A2 slide 25: "a DAO query deletes records after N days"). */
object RetentionPolicy {

    /**
     * Records with a timestamp strictly before the returned value must be deleted.
     * Returns null for [RetentionPeriod.FOREVER] (nothing is deleted).
     */
    fun cutoffMillis(period: RetentionPeriod, nowMillis: Long): Long? = TODO("Stage 3")
}
