package com.yibopan.quizquest.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yibopan.quizquest.data.database.QuestionEntity
import com.yibopan.quizquest.data.database.QuizDatabase
import com.yibopan.quizquest.data.model.AnswerRecord
import com.yibopan.quizquest.data.model.Difficulty
import com.yibopan.quizquest.data.model.QuizResult
import com.yibopan.quizquest.data.model.RetentionPeriod
import com.yibopan.quizquest.data.model.TimerMode
import com.yibopan.quizquest.data.model.UserSettings
import com.yibopan.quizquest.data.network.CategoriesResponse
import com.yibopan.quizquest.data.network.NetworkResult
import com.yibopan.quizquest.data.network.QuestionsResponse
import com.yibopan.quizquest.data.network.RateLimiter
import com.yibopan.quizquest.data.network.TokenResponse
import com.yibopan.quizquest.data.network.TriviaAPI
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response
import java.io.IOException

/** Instrumented integration tests for repositories backed by an in-memory Room database. */
@RunWith(AndroidJUnit4::class)
class RoomRepositoryIntegrationTest {

    private lateinit var db: QuizDatabase
    private lateinit var triviaRepository: TriviaRepositoryImpl
    private lateinit var progressRepository: ProgressRepositoryImpl
    private lateinit var reviewRepository: ReviewRepositoryImpl
    private lateinit var settingsRepository: SettingsRepositoryImpl
    private lateinit var testTriviaAPI: TestTriviaAPI

    class TestTriviaAPI : TriviaAPI {
        var shouldFail = false

        override suspend fun fetchQuestions(
            amount: Int,
            category: Int,
            difficulty: String,
            token: String?,
            encode: String,
        ): Response<QuestionsResponse> {
            if (shouldFail) throw IOException("Offline")
            return Response.success(QuestionsResponse(0, emptyList()))
        }

        override suspend fun fetchCategories(): Response<CategoriesResponse> {
            return Response.success(CategoriesResponse(emptyList()))
        }

        override suspend fun requestToken(command: String): Response<TokenResponse> {
            return Response.success(TokenResponse(0, token = "test_token"))
        }

        override suspend fun resetToken(token: String, command: String): Response<TokenResponse> {
            return Response.success(TokenResponse(0, token = "reset_token"))
        }
    }

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, QuizDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        testTriviaAPI = TestTriviaAPI()

        triviaRepository = TriviaRepositoryImpl(
            triviaAPI = testTriviaAPI,
            rateLimiter = RateLimiter(minIntervalMillis = 0L),
            categoryDao = db.categoryDao(),
            questionDao = db.questionDao(),
            flaggedQuestionDao = db.flaggedQuestionDao(),
            timeProvider = { 1000L },
        )

        progressRepository = ProgressRepositoryImpl(
            levelProgressDao = db.levelProgressDao(),
            quizSessionDao = db.quizSessionDao(),
            answerRecordDao = db.answerRecordDao(),
            timeProvider = { 1000L },
        )

        reviewRepository = ReviewRepositoryImpl(
            reviewCardDao = db.reviewCardDao(),
        )

        settingsRepository = SettingsRepositoryImpl(
            settingsDao = db.settingsDao(),
        )
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun offlineQuestionsLoadedFromRoomWhenNetworkFails() = runBlocking {
        testTriviaAPI.shouldFail = true
        db.questionDao().insertAll(
            listOf(
                QuestionEntity(
                    id = 1L,
                    categoryId = 17,
                    difficulty = "easy",
                    type = "multiple",
                    text = "Offline H2O?",
                    correctAnswer = "Water",
                    incorrectAnswers = listOf("Air"),
                    fetchedAt = 1000L,
                    used = false,
                ),
            ),
        )

        val result = triviaRepository.loadQuestions(categoryId = 17, difficulty = Difficulty.EASY, amount = 1)

        assertTrue(result is NetworkResult.Success)
        val questions = (result as NetworkResult.Success).data
        assertEquals("Offline H2O?", questions[0].text)
    }

    @Test
    fun reportedQuestionIsNeverShownAgainFromRoom() = runBlocking {
        testTriviaAPI.shouldFail = true
        db.questionDao().insertAll(
            listOf(
                QuestionEntity(id = 1L, categoryId = 17, difficulty = "easy", type = "multiple", text = "Bad Question", correctAnswer = "A", incorrectAnswers = listOf("B"), fetchedAt = 1000L, used = false),
                QuestionEntity(id = 2L, categoryId = 17, difficulty = "easy", type = "multiple", text = "Good Question", correctAnswer = "A", incorrectAnswers = listOf("B"), fetchedAt = 1000L, used = false),
            ),
        )

        triviaRepository.flagQuestion("Bad Question")

        val result = triviaRepository.loadQuestions(categoryId = 17, difficulty = Difficulty.EASY, amount = 10)

        assertTrue(result is NetworkResult.Success)
        val questions = (result as NetworkResult.Success).data
        assertEquals(1, questions.size)
        assertEquals("Good Question", questions[0].text)
    }

    @Test
    fun saveQuizResultAndRetrieveProgressFromRoom() = runBlocking {
        val result = QuizResult(
            sessionId = 0L,
            categoryId = 17,
            level = 1,
            correctCount = 8,
            totalCount = 10,
            stars = 2,
            startedAt = 1000L,
            durationMs = 20000L,
        )
        val answers = listOf(
            AnswerRecord("Q1", "A", "A", true, 2000L),
        )

        val sessionId = progressRepository.saveQuizResult(result, answers)

        val savedResult = progressRepository.getQuizResult(sessionId).first()
        assertNotNull(savedResult)
        assertEquals(2, savedResult?.stars)

        val progressList = progressRepository.getLevelProgress(17).first()
        assertEquals(1, progressList.size)
        assertEquals(2, progressList[0].stars)
    }

    @Test
    fun addWrongAnswersCreatesReviewCardsInRoom() = runBlocking {
        val answers = listOf(
            AnswerRecord("Q1", "B", "A", false, 1500L),
        )

        reviewRepository.addWrongAnswers(answers, nowMillis = 1000L)

        val dueCount = reviewRepository.getDueCount(nowMillis = 1000L + 90_000_000L).first()
        assertEquals(1, dueCount)

        val dueCards = reviewRepository.getDueCards(nowMillis = 1000L + 90_000_000L).first()
        assertEquals("Q1", dueCards[0].questionText)
        assertEquals(1, dueCards[0].box)
    }

    @Test
    fun updateSettingsPersistsInRoom() = runBlocking {
        val newSettings = UserSettings(
            questionsPerLevel = 15,
            timerMode = TimerMode.CHALLENGE,
            answerRetention = RetentionPeriod.DAYS_30,
        )

        settingsRepository.updateSettings(newSettings)

        val loaded = settingsRepository.getSettings().first()
        assertEquals(15, loaded.questionsPerLevel)
        assertEquals(TimerMode.CHALLENGE, loaded.timerMode)
        assertEquals(RetentionPeriod.DAYS_30, loaded.answerRetention)
    }
}
