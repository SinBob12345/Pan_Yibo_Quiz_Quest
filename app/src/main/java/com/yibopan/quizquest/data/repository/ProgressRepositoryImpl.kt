package com.yibopan.quizquest.data.repository

import com.yibopan.quizquest.data.database.AnswerRecordDao
import com.yibopan.quizquest.data.database.LevelProgressDao
import com.yibopan.quizquest.data.database.LevelProgressEntity
import com.yibopan.quizquest.data.database.QuizSessionDao
import com.yibopan.quizquest.data.database.toEntity
import com.yibopan.quizquest.data.database.toLevelProgress
import com.yibopan.quizquest.data.database.toQuizResult
import com.yibopan.quizquest.data.model.AnswerRecord
import com.yibopan.quizquest.data.model.LevelProgress
import com.yibopan.quizquest.data.model.QuizResult
import com.yibopan.quizquest.data.rules.StarRating
import com.yibopan.quizquest.data.rules.SystemTimeProvider
import com.yibopan.quizquest.data.rules.TimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Stores progress and quiz sessions in Room (Lecture 7).
 *
 * Keeps best stars and best score per level (never lowers them).
 * Unlocking is NOT stored: WorldMapViewModel works it out with LevelPolicy.
 */
class ProgressRepositoryImpl(
    private val levelProgressDao: LevelProgressDao,
    private val quizSessionDao: QuizSessionDao,
    private val answerRecordDao: AnswerRecordDao,
    private val timeProvider: TimeProvider = SystemTimeProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ProgressRepository {

    override suspend fun getLevelProgress(categoryId: Int): Flow<List<LevelProgress>> {
        return levelProgressDao.getLevelProgressForCategory(categoryId)
            .map { entities -> entities.map { it.toLevelProgress() } }
    }

    override suspend fun saveQuizResult(result: QuizResult, answers: List<AnswerRecord>): Long = withContext(dispatcher) {
        val now = timeProvider.now()
        val sessionEntity = result.toEntity()
        val sessionId = quizSessionDao.insert(sessionEntity)

        val answerEntities = answers.map { it.toEntity(sessionId = sessionId, answeredAt = now) }
        answerRecordDao.insertAll(answerEntities)

        val existing = levelProgressDao.getProgress(result.categoryId, result.level)
        val bestStars = maxOf(result.stars, existing?.stars ?: 0)
        val bestScore = maxOf(result.correctCount, existing?.bestScore ?: 0)

        val progressEntity = LevelProgressEntity(
            categoryId = result.categoryId,
            level = result.level,
            stars = bestStars,
            bestScore = bestScore,
            updatedAt = now,
        )
        levelProgressDao.insert(progressEntity)

        sessionId
    }

    override suspend fun getQuizResult(sessionId: Long): Flow<QuizResult?> {
        return quizSessionDao.getAllSessions()
            .map { sessions -> sessions.find { it.id == sessionId }?.toQuizResult() }
    }

    override suspend fun getLevelsCompletedSince(startMillis: Long): Flow<Int> {
        return quizSessionDao.getAllSessions().map { sessions ->
            sessions.count { it.startedAt >= startMillis && it.stars >= StarRating.PASS_STARS }
        }
    }
}
