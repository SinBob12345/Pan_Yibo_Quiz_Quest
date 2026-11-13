package com.yibopan.quizquest.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Instrumented unit tests for [QuizDatabase] DAOs using an in-memory Room database. */
@RunWith(AndroidJUnit4::class)
class QuizDatabaseTest {

    private lateinit var db: QuizDatabase

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            QuizDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetCategories() = runBlocking {
        val categoryDao = db.categoryDao()
        val categories = listOf(
            CategoryEntity(id = 9, name = "General Knowledge", updatedAt = 1000L),
            CategoryEntity(id = 17, name = "Science & Nature", updatedAt = 1000L),
        )

        categoryDao.insertAll(categories)
        val loaded = categoryDao.getCategories().first()

        assertEquals(2, loaded.size)
        assertEquals("General Knowledge", loaded[0].name)
    }

    @Test
    fun insertAndGetQuestions() = runBlocking {
        val questionDao = db.questionDao()
        val questions = listOf(
            QuestionEntity(
                id = 1L,
                categoryId = 17,
                difficulty = "easy",
                type = "multiple",
                text = "What is H2O?",
                correctAnswer = "Water",
                incorrectAnswers = listOf("Fire", "Earth", "Air"),
                fetchedAt = 2000L,
            ),
        )

        questionDao.insertAll(questions)
        val loaded = questionDao.getQuestions(categoryId = 17, difficulty = "easy", amount = 10)

        assertEquals(1, loaded.size)
        assertEquals("What is H2O?", loaded[0].text)
    }

    @Test
    fun getUnusedQuestionsFiltersOutUsedAndFlaggedQuestions() = runBlocking {
        val questionDao = db.questionDao()
        val flaggedDao = db.flaggedQuestionDao()

        val q1 = QuestionEntity(id = 1L, categoryId = 17, difficulty = "easy", type = "multiple", text = "Q1", correctAnswer = "A", incorrectAnswers = listOf("B"), fetchedAt = 1000L, used = false)
        val q2 = QuestionEntity(id = 2L, categoryId = 17, difficulty = "easy", type = "multiple", text = "Q2", correctAnswer = "A", incorrectAnswers = listOf("B"), fetchedAt = 1000L, used = true)
        val q3 = QuestionEntity(id = 3L, categoryId = 17, difficulty = "easy", type = "multiple", text = "Q3", correctAnswer = "A", incorrectAnswers = listOf("B"), fetchedAt = 1000L, used = false)

        questionDao.insertAll(listOf(q1, q2, q3))
        flaggedDao.insert(FlaggedQuestionEntity("Q3", flaggedAt = 2000L))

        val unused = questionDao.getUnusedQuestions(categoryId = 17, difficulty = "easy", amount = 10)

        assertEquals(1, unused.size)
        assertEquals("Q1", unused[0].text)
    }

    @Test
    fun markAsUsedUpdatesQuestionStatus() = runBlocking {
        val questionDao = db.questionDao()
        val q1 = QuestionEntity(id = 1L, categoryId = 17, difficulty = "easy", type = "multiple", text = "Q1", correctAnswer = "A", incorrectAnswers = listOf("B"), fetchedAt = 1000L, used = false)
        questionDao.insertAll(listOf(q1))

        questionDao.markAsUsed(listOf(1L))
        val unused = questionDao.getUnusedQuestions(categoryId = 17, difficulty = "easy", amount = 10)

        assertTrue(unused.isEmpty())
    }

    @Test
    fun cascadeDeleteSessionDeletesAnswerRecords() = runBlocking {
        val sessionDao = db.quizSessionDao()
        val answerDao = db.answerRecordDao()

        val sessionId = sessionDao.insert(
            QuizSessionEntity(id = 0L, categoryId = 17, level = 1, startedAt = 1000L, durationMs = 5000L, correctCount = 5, totalCount = 10, stars = 1),
        )

        val answer = AnswerRecordEntity(id = 0L, sessionId = sessionId, questionText = "Q1", chosenAnswer = "A", correctAnswer = "A", isCorrect = true, answeredAt = 1050L, timeTakenMs = 1200L)
        answerDao.insertAll(listOf(answer))

        assertEquals(1, answerDao.getAnswersForSession(sessionId).size)

        sessionDao.deleteAll()

        assertTrue(answerDao.getAnswersForSession(sessionId).isEmpty())
    }

    @Test
    fun getSessionStartTimesReturnsCorrectSessionsInOrder() = runBlocking {
        val sessionDao = db.quizSessionDao()
        sessionDao.insert(QuizSessionEntity(id = 0L, categoryId = 17, level = 1, startedAt = 1000L, durationMs = 5000L, correctCount = 0, totalCount = 10, stars = 0))
        sessionDao.insert(QuizSessionEntity(id = 0L, categoryId = 17, level = 2, startedAt = 2000L, durationMs = 5000L, correctCount = 8, totalCount = 10, stars = 2))
        sessionDao.insert(QuizSessionEntity(id = 0L, categoryId = 17, level = 3, startedAt = 3000L, durationMs = 5000L, correctCount = 6, totalCount = 10, stars = 1))

        val times = sessionDao.getSessionStartTimes().first()

        assertEquals(listOf(3000L, 2000L), times)
    }

    @Test
    fun deleteOlderThanRemovesExpiredAnswerRecords() = runBlocking {
        val sessionDao = db.quizSessionDao()
        val answerDao = db.answerRecordDao()

        val sessionId = sessionDao.insert(QuizSessionEntity(id = 0L, categoryId = 17, level = 1, startedAt = 1000L, durationMs = 5000L, correctCount = 5, totalCount = 10, stars = 1))

        val oldRecord = AnswerRecordEntity(id = 0L, sessionId = sessionId, questionText = "Old Q", chosenAnswer = "A", correctAnswer = "A", isCorrect = true, answeredAt = 1000L, timeTakenMs = 1000L)
        val newRecord = AnswerRecordEntity(id = 0L, sessionId = sessionId, questionText = "New Q", chosenAnswer = "A", correctAnswer = "A", isCorrect = true, answeredAt = 5000L, timeTakenMs = 1000L)
        answerDao.insertAll(listOf(oldRecord, newRecord))

        val deletedCount = answerDao.deleteOlderThan(cutoffMillis = 3000L)

        assertEquals(1, deletedCount)
        val remaining = answerDao.getAnswersForSession(sessionId)
        assertEquals(1, remaining.size)
        assertEquals("New Q", remaining[0].questionText)
    }

    @Test
    fun dueReviewCardsOrderedByNextReviewAt() = runBlocking {
        val reviewCardDao = db.reviewCardDao()

        val dueCard1 = ReviewCardEntity(id = 0L, questionText = "Q1", correctAnswer = "A", box = 1, nextReviewAt = 1000L, timesWrong = 1, createdAt = 500L)
        val dueCard2 = ReviewCardEntity(id = 0L, questionText = "Q2", correctAnswer = "A", box = 2, nextReviewAt = 2000L, timesWrong = 1, createdAt = 500L)
        val futureCard = ReviewCardEntity(id = 0L, questionText = "Q3", correctAnswer = "A", box = 1, nextReviewAt = 10000L, timesWrong = 1, createdAt = 500L)
        reviewCardDao.insert(dueCard2)
        reviewCardDao.insert(dueCard1)
        reviewCardDao.insert(futureCard)

        val dueCards = reviewCardDao.getDueReviewCards(nowMillis = 5000L).first()

        assertEquals(2, dueCards.size)
        assertEquals("Q1", dueCards[0].questionText)
        assertEquals("Q2", dueCards[1].questionText)
    }

    @Test
    fun insertAndUpdateSettings() = runBlocking {
        val settingsDao = db.settingsDao()
        val settings = SettingsEntity(
            id = 0,
            questionsPerLevel = 10,
            timerMode = "OFF",
            soundEnabled = true,
            reduceMotion = false,
            themeMode = "SYSTEM",
            dailyGoalLevels = 3,
            answerRetention = "DAYS_90",
        )

        settingsDao.insertOrUpdate(settings)
        val loaded = settingsDao.getSettingsDirect()

        assertNotNull(loaded)
        assertEquals(10, loaded?.questionsPerLevel)
        assertEquals("DAYS_90", loaded?.answerRetention)

        val updated = settings.copy(questionsPerLevel = 15, timerMode = "CHALLENGE")
        settingsDao.insertOrUpdate(updated)

        val reloaded = settingsDao.getSettings().first()
        assertNotNull(reloaded)
        assertEquals(15, reloaded?.questionsPerLevel)
        assertEquals("CHALLENGE", reloaded?.timerMode)
    }

    @Test
    fun insertAndGetLevelProgress() = runBlocking {
        val progressDao = db.levelProgressDao()
        val progress = LevelProgressEntity(categoryId = 17, level = 1, stars = 3, bestScore = 10, updatedAt = 3000L)

        progressDao.insert(progress)
        val loaded = progressDao.getProgress(categoryId = 17, level = 1)

        assertNotNull(loaded)
        assertEquals(3, loaded?.stars)
    }
}
