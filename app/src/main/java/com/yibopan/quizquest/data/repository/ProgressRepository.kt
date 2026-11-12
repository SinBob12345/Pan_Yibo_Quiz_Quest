package com.yibopan.quizquest.data.repository

import com.yibopan.quizquest.data.model.AnswerRecord
import com.yibopan.quizquest.data.model.LevelProgress
import com.yibopan.quizquest.data.model.QuizResult
import kotlinx.coroutines.flow.Flow

/** Level progress and finished quiz sessions, stored in Room. Implemented in Stage 4. */
interface ProgressRepository {

    /** Stored progress of the played levels in one world, ordered by level number. */
    suspend fun getLevelProgress(categoryId: Int): Flow<List<LevelProgress>>

    /**
     * Saves a finished session and its answers, keeps the level's best stars/score
     * (never lowers them) and returns the new session id.
     */
    suspend fun saveQuizResult(result: QuizResult, answers: List<AnswerRecord>): Long

    /** A saved session, or null if it no longer exists (e.g. after "delete all my data"). */
    suspend fun getQuizResult(sessionId: Long): Flow<QuizResult?>

    /** Number of levels passed (stars >= StarRating.PASS_STARS) since [startMillis]; used for the daily goal. */
    suspend fun getLevelsCompletedSince(startMillis: Long): Flow<Int>
}
