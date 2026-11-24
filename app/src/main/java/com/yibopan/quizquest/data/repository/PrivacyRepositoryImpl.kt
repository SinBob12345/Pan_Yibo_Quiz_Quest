package com.yibopan.quizquest.data.repository

import com.yibopan.quizquest.data.database.AnswerRecordDao
import com.yibopan.quizquest.data.database.FlaggedQuestionDao
import com.yibopan.quizquest.data.database.LevelProgressDao
import com.yibopan.quizquest.data.database.QuizDatabase
import com.yibopan.quizquest.data.database.QuizSessionDao
import com.yibopan.quizquest.data.database.ReviewCardDao
import com.yibopan.quizquest.data.database.SettingsDao
import com.yibopan.quizquest.data.database.toAnswerRecord
import com.yibopan.quizquest.data.database.toLevelProgress
import com.yibopan.quizquest.data.database.toQuizResult
import com.yibopan.quizquest.data.database.toReviewCard
import com.yibopan.quizquest.data.database.toUserSettings
import com.yibopan.quizquest.data.export.ExportSerializer
import com.yibopan.quizquest.data.export.ReportedQuestion
import com.yibopan.quizquest.data.export.UserDataExport
import com.yibopan.quizquest.data.model.UserSettings
import com.yibopan.quizquest.data.rules.RetentionPolicy
import com.yibopan.quizquest.data.rules.SystemTimeProvider
import com.yibopan.quizquest.data.rules.TimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Learner data privacy, export, and retention repository (A2 Commitment #2).
 */
class PrivacyRepositoryImpl(
    private val database: QuizDatabase,
    private val settingsDao: SettingsDao,
    private val levelProgressDao: LevelProgressDao,
    private val quizSessionDao: QuizSessionDao,
    private val answerRecordDao: AnswerRecordDao,
    private val reviewCardDao: ReviewCardDao,
    private val flaggedQuestionDao: FlaggedQuestionDao,
    private val appVersion: String = "1.0.0",
    private val timeProvider: TimeProvider = SystemTimeProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : PrivacyRepository {

    override suspend fun exportAllDataAsJson(): String = withContext(dispatcher) {
        val now = timeProvider.now()
        val settings = settingsDao.getSettings().first()?.toUserSettings()
            ?: settingsDao.getSettingsDirect()?.toUserSettings()
            ?: UserSettings()

        val progressList = levelProgressDao.getAllProgress().first().map { it.toLevelProgress() }
        val sessionsList = quizSessionDao.getAllSessions().first().map { it.toQuizResult() }
        val answersList = sessionsList.flatMap { session ->
            answerRecordDao.getAnswersForSession(session.sessionId).map { it.toAnswerRecord() }
        }
        val reviewCardsList = reviewCardDao.getAllReviewCards().first().map { it.toReviewCard() }
        val reportedQuestionsList = flaggedQuestionDao.getAll().map {
            ReportedQuestion(questionText = it.questionText, reportedAt = it.flaggedAt)
        }

        val exportContainer = UserDataExport(
            exportedAt = now,
            appVersion = appVersion,
            settings = settings,
            levelProgress = progressList,
            sessions = sessionsList,
            answers = answersList,
            reviewCards = reviewCardsList,
            reportedQuestions = reportedQuestionsList,
        )

        ExportSerializer.toJson(exportContainer)
    }

    override suspend fun deleteAllData(): Unit = withContext(dispatcher) {
        database.clearAllTables()
    }

    override suspend fun deleteExpiredAnswers(nowMillis: Long): Int = withContext(dispatcher) {
        val settings = settingsDao.getSettingsDirect()?.toUserSettings()
            ?: UserSettings()
        val cutoff = RetentionPolicy.cutoffMillis(settings.answerRetention, nowMillis) ?: return@withContext 0
        answerRecordDao.deleteOlderThan(cutoff)
    }

    override fun observeReportedCount(): Flow<Int> {
        return flaggedQuestionDao.observeCount()
    }

    override suspend fun restoreReportedQuestions(): Unit = withContext(dispatcher) {
        flaggedQuestionDao.deleteAll()
    }
}
