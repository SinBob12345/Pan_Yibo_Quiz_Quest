package com.yibopan.quizquest.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * The learner's control over their own data (A2 commitment #2, ACS Ethics 2.2(d)).
 * Implemented in Stage 7.
 */
interface PrivacyRepository {

    /** Everything the app stores about the learner, as pretty-printed JSON (kotlinx.serialization, Lecture 5). */
    suspend fun exportAllDataAsJson(): String

    /** Deletes every table in the Room database (scores, answers, cards, settings). Cannot be undone. */
    suspend fun deleteAllData()

    /** Deletes answer records older than the chosen retention period. Returns the number of rows deleted. */
    suspend fun deleteExpiredAnswers(nowMillis: Long): Int

    /** Observes the current count of reported questions in the database. */
    fun observeReportedCount(): Flow<Int>

    /** Clears all reported questions so they can appear in quizzes again. */
    suspend fun restoreReportedQuestions()
}
