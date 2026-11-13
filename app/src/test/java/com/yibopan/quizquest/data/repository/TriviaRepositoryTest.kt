package com.yibopan.quizquest.data.repository

import com.yibopan.quizquest.data.model.Difficulty
import com.yibopan.quizquest.data.network.NetworkResult
import com.yibopan.quizquest.data.network.OpenTdbResponseCode
import com.yibopan.quizquest.data.network.RateLimiter
import com.yibopan.quizquest.fakes.FakeTriviaAPI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Unit tests for [TriviaRepositoryImpl] using [FakeTriviaAPI] (Lecture 5 / Lecture 4 repository testing). */
@OptIn(ExperimentalCoroutinesApi::class)
class TriviaRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeTriviaAPI: FakeTriviaAPI
    private lateinit var rateLimiter: RateLimiter
    private lateinit var repository: TriviaRepositoryImpl

    @Before
    fun setUp() {
        fakeTriviaAPI = FakeTriviaAPI()
        rateLimiter = RateLimiter(minIntervalMillis = 0L)
        repository = TriviaRepositoryImpl(
            triviaAPI = fakeTriviaAPI,
            rateLimiter = rateLimiter,
            dispatcher = testDispatcher,
        )
    }

    @Test
    fun `getCategories returns decoded allowed academic categories`() = runTest {
        val categories = repository.getCategories().first()

        assertEquals(3, categories.size)
        assertEquals(listOf(9, 17, 23), categories.map { it.id })
        assertEquals(listOf("General Knowledge", "Science & Nature", "History"), categories.map { it.name })
    }

    @Test
    fun `loadQuestions succeeds and decodes question text`() = runTest {
        val result = repository.loadQuestions(categoryId = 17, difficulty = Difficulty.EASY, amount = 1)

        assertTrue(result is NetworkResult.Success)
        val questions = (result as NetworkResult.Success).data
        assertEquals(1, questions.size)
        assertEquals("What is H2O?", questions[0].text)
        assertEquals("Water", questions[0].correctAnswer)
    }

    @Test
    fun `loadQuestions requests token before calling API`() = runTest {
        repository.loadQuestions(categoryId = 17, difficulty = Difficulty.EASY, amount = 1)

        assertEquals(1, fakeTriviaAPI.requestedTokenCount)
        assertEquals("fake_token_1", fakeTriviaAPI.lastRequestedToken)
    }

    @Test
    fun `loadQuestions handles token expired code 3 by refreshing token and retrying`() = runTest {
        fakeTriviaAPI.responseCodesSequence.add(OpenTdbResponseCode.TOKEN_NOT_FOUND)
        fakeTriviaAPI.responseCodesSequence.add(OpenTdbResponseCode.SUCCESS)

        val result = repository.loadQuestions(categoryId = 17, difficulty = Difficulty.EASY, amount = 1)

        assertTrue(result is NetworkResult.Success)
        assertEquals(2, fakeTriviaAPI.requestedTokenCount)
    }

    @Test
    fun `loadQuestions handles token empty code 4 by resetting token and retrying`() = runTest {
        fakeTriviaAPI.responseCodesSequence.add(OpenTdbResponseCode.TOKEN_EMPTY)
        fakeTriviaAPI.responseCodesSequence.add(OpenTdbResponseCode.SUCCESS)

        val result = repository.loadQuestions(categoryId = 17, difficulty = Difficulty.EASY, amount = 1)

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, fakeTriviaAPI.resetTokenCount)
    }

    @Test
    fun `loadQuestions returns error result when rate limited`() = runTest {
        fakeTriviaAPI.questionsResponseCode = OpenTdbResponseCode.RATE_LIMIT

        val result = repository.loadQuestions(categoryId = 17, difficulty = Difficulty.EASY, amount = 1)

        assertTrue(result is NetworkResult.Error)
        assertEquals("Rate limit reached. Please try again in a few seconds.", (result as NetworkResult.Error).error)
    }

    @Test
    fun `loadQuestions returns error result on network exception`() = runTest {
        fakeTriviaAPI.shouldQuestionsFailNetwork = true

        val result = repository.loadQuestions(categoryId = 17, difficulty = Difficulty.EASY, amount = 1)

        assertTrue(result is NetworkResult.Error)
        assertEquals("Network offline", (result as NetworkResult.Error).error)
    }

    @Test
    fun `flagQuestion filters flagged questions from loadQuestions`() = runTest {
        repository.flagQuestion("What is H2O?")

        val result = repository.loadQuestions(categoryId = 17, difficulty = Difficulty.EASY, amount = 1)

        assertTrue(result is NetworkResult.Success)
        val questions = (result as NetworkResult.Success).data
        assertTrue(questions.isEmpty())
    }
}
