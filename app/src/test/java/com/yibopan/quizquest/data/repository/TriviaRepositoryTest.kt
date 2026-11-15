package com.yibopan.quizquest.data.repository

import com.yibopan.quizquest.data.database.CategoryEntity
import com.yibopan.quizquest.data.database.FlaggedQuestionEntity
import com.yibopan.quizquest.data.database.QuestionEntity
import com.yibopan.quizquest.data.model.Difficulty
import com.yibopan.quizquest.data.network.NetworkResult
import com.yibopan.quizquest.data.network.RateLimiter
import com.yibopan.quizquest.fakes.FakeCategoryDao
import com.yibopan.quizquest.fakes.FakeFlaggedQuestionDao
import com.yibopan.quizquest.fakes.FakeQuestionDao
import com.yibopan.quizquest.fakes.FakeTriviaAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Unit tests for offline-first [TriviaRepositoryImpl] (Lecture 7). */
@OptIn(ExperimentalCoroutinesApi::class)
class TriviaRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeTriviaAPI: FakeTriviaAPI
    private lateinit var categoryDao: FakeCategoryDao
    private lateinit var questionDao: FakeQuestionDao
    private lateinit var flaggedDao: FakeFlaggedQuestionDao
    private lateinit var repository: TriviaRepositoryImpl

    @Before
    fun setUp() {
        fakeTriviaAPI = FakeTriviaAPI()
        categoryDao = FakeCategoryDao()
        flaggedDao = FakeFlaggedQuestionDao()
        questionDao = FakeQuestionDao(flaggedDao)

        repository = TriviaRepositoryImpl(
            triviaAPI = fakeTriviaAPI,
            rateLimiter = RateLimiter(minIntervalMillis = 0L),
            categoryDao = categoryDao,
            questionDao = questionDao,
            flaggedQuestionDao = flaggedDao,
            dispatcher = testDispatcher,
        )
    }

    @Test
    fun `getCategories fetches from network when Room is empty and saves to Room`() = runTest {
        val categories = repository.getCategories().first()

        assertEquals(3, categories.size)
        assertEquals(listOf(9, 17, 23), categories.map { it.id })

        val savedInRoom = categoryDao.getCategories().first()
        assertEquals(3, savedInRoom.size)
    }

    @Test
    fun `getCategories uses Room when categories are already stored`() = runTest {
        categoryDao.insertAll(
            listOf(
                CategoryEntity(id = 9, name = "General Knowledge", updatedAt = 100L),
                CategoryEntity(id = 17, name = "Science & Nature", updatedAt = 100L),
            ),
        )

        val categories = repository.getCategories().first()

        assertEquals(2, categories.size)
        assertEquals("General Knowledge", categories[0].name)
    }

    @Test
    fun `loadQuestions saves downloaded questions to Room`() = runTest {
        val result = repository.loadQuestions(categoryId = 17, difficulty = Difficulty.EASY, amount = 1)

        assertTrue(result is NetworkResult.Success)
        val questions = (result as NetworkResult.Success).data
        assertEquals(1, questions.size)

        assertEquals(1, questionDao.questions.size)
        assertTrue(questionDao.questions[0].used)
    }

    @Test
    fun `loadQuestions falls back to Room when network is offline`() = runTest {
        fakeTriviaAPI.shouldQuestionsFailNetwork = true
        questionDao.insertAll(
            listOf(
                QuestionEntity(
                    id = 100L,
                    categoryId = 17,
                    difficulty = "easy",
                    type = "multiple",
                    text = "Offline Question?",
                    correctAnswer = "Yes",
                    incorrectAnswers = listOf("No"),
                    fetchedAt = 1000L,
                    used = false,
                ),
            ),
        )

        val result = repository.loadQuestions(categoryId = 17, difficulty = Difficulty.EASY, amount = 1)

        assertTrue(result is NetworkResult.Success)
        val questions = (result as NetworkResult.Success).data
        assertEquals("Offline Question?", questions[0].text)
        assertTrue(questionDao.questions[0].used)
    }

    @Test
    fun `loadQuestions filters out reported questions when loading offline questions`() = runTest {
        fakeTriviaAPI.shouldQuestionsFailNetwork = true
        questionDao.insertAll(
            listOf(
                QuestionEntity(id = 100L, categoryId = 17, difficulty = "easy", type = "multiple", text = "Good Question", correctAnswer = "A", incorrectAnswers = listOf("B"), fetchedAt = 1000L, used = false),
                QuestionEntity(id = 101L, categoryId = 17, difficulty = "easy", type = "multiple", text = "Reported Question", correctAnswer = "A", incorrectAnswers = listOf("B"), fetchedAt = 1000L, used = false),
            ),
        )
        flaggedDao.insert(FlaggedQuestionEntity("Reported Question", 2000L))

        val result = repository.loadQuestions(categoryId = 17, difficulty = Difficulty.EASY, amount = 10)

        assertTrue(result is NetworkResult.Success)
        val questions = (result as NetworkResult.Success).data
        assertEquals(1, questions.size)
        assertEquals("Good Question", questions[0].text)
    }

    @Test
    fun `loadQuestions returns error when network is offline and Room has no questions`() = runTest {
        fakeTriviaAPI.shouldQuestionsFailNetwork = true

        val result = repository.loadQuestions(categoryId = 17, difficulty = Difficulty.EASY, amount = 1)

        assertTrue(result is NetworkResult.Error)
        assertEquals("No questions available offline. Please check your internet connection.", (result as NetworkResult.Error).error)
    }

    @Test
    fun `flagQuestion saves flagged question to Room`() = runTest {
        repository.flagQuestion("Reported Question Text")

        val flagged = flaggedDao.getFlaggedQuestionTexts().first()
        assertEquals(listOf("Reported Question Text"), flagged)
    }
}
