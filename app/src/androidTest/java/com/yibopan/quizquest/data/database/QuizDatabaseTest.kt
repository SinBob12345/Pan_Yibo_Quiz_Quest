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
    fun insertAndGetSettings() = runBlocking {
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

    @Test
    fun insertAndGetQuizSessionWithAnswers() = runBlocking {
        val sessionDao = db.quizSessionDao()
        val answerDao = db.answerRecordDao()

        val session = QuizSessionEntity(
            id = 0L,
            categoryId = 17,
            level = 1,
            startedAt = 4000L,
            durationMs = 30000L,
            correctCount = 8,
            totalCount = 10,
            stars = 2,
        )
        val sessionId = sessionDao.insert(session)

        val answers = listOf(
            AnswerRecordEntity(
                id = 0L,
                sessionId = sessionId,
                questionText = "Q1",
                chosenAnswer = "A1",
                correctAnswer = "A1",
                isCorrect = true,
                answeredAt = 4010L,
                timeTakenMs = 2000L,
            ),
        )
        answerDao.insertAll(answers)

        val loadedAnswers = answerDao.getAnswersForSession(sessionId)
        assertEquals(1, loadedAnswers.size)
        assertTrue(loadedAnswers[0].isCorrect)
    }

    @Test
    fun insertAndGetReviewCard() = runBlocking {
        val cardDao = db.reviewCardDao()
        val card = ReviewCardEntity(
            id = 0L,
            questionText = "Wrong Q",
            correctAnswer = "Correct A",
            box = 1,
            nextReviewAt = 5000L,
            timesWrong = 1,
            createdAt = 4500L,
        )

        cardDao.insert(card)
        val loaded = cardDao.getDueReviewCards(nowMillis = 6000L).first()

        assertEquals(1, loaded.size)
        assertEquals("Wrong Q", loaded[0].questionText)
    }
}
