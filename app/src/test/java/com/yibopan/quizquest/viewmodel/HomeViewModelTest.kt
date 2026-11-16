package com.yibopan.quizquest.viewmodel

import com.yibopan.quizquest.data.model.AnswerRecord
import com.yibopan.quizquest.data.model.QuizResult
import com.yibopan.quizquest.data.model.UserSettings
import com.yibopan.quizquest.fakes.FakeProgressRepository
import com.yibopan.quizquest.fakes.FakeReviewRepository
import com.yibopan.quizquest.fakes.FakeSettingsRepository
import com.yibopan.quizquest.fakes.FakeStatsRepository
import com.yibopan.quizquest.fakes.FakeTriviaRepository
import com.yibopan.quizquest.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Unit tests for [HomeViewModel] (Lecture 4 / Lecture 5). */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var triviaRepo: FakeTriviaRepository
    private lateinit var progressRepo: FakeProgressRepository
    private lateinit var reviewRepo: FakeReviewRepository
    private lateinit var statsRepo: FakeStatsRepository
    private lateinit var settingsRepo: FakeSettingsRepository

    @Before
    fun setUp() {
        triviaRepo = FakeTriviaRepository()
        progressRepo = FakeProgressRepository()
        reviewRepo = FakeReviewRepository()
        statsRepo = FakeStatsRepository()
        settingsRepo = FakeSettingsRepository()
    }

    @Test
    fun `initial state collects repository data and maps uiState`() = runTest {
        val viewModel = HomeViewModel(
            triviaRepository = triviaRepo,
            progressRepository = progressRepo,
            reviewRepository = reviewRepo,
            statsRepository = statsRepo,
            settingsRepository = settingsRepo,
            timeProvider = { 1000L },
        )

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(3, state.worlds.size)
        assertEquals(3, state.dailyGoal)
        assertEquals(0, state.levelsCompletedToday)
        assertFalse(state.isDailyGoalReached)
        assertEquals(0, state.dueReviewCount)
    }

    @Test
    fun `detects when daily goal is reached on home screen`() = runTest {
        settingsRepo.updateSettings(UserSettings(dailyGoalLevels = 2))

        val result1 = QuizResult(0L, 17, 1, 8, 10, 2, 1000L, 10000L)
        val result2 = QuizResult(0L, 17, 2, 9, 10, 2, 2000L, 10000L)

        progressRepo.saveQuizResult(result1, emptyList())
        progressRepo.saveQuizResult(result2, emptyList())

        val viewModel = HomeViewModel(
            triviaRepository = triviaRepo,
            progressRepository = progressRepo,
            reviewRepository = reviewRepo,
            statsRepository = statsRepo,
            settingsRepository = settingsRepo,
            timeProvider = { 3000L },
        )

        val state = viewModel.uiState.value
        assertEquals(2, state.levelsCompletedToday)
        assertTrue(state.isDailyGoalReached)
    }

    @Test
    fun `displays due review card count`() = runTest {
        val now = 1_000_000_000L
        reviewRepo.addWrongAnswers(
            listOf(AnswerRecord("Wrong Q", "B", "A", false, 1000L)),
            nowMillis = now,
        )

        val viewModel = HomeViewModel(
            triviaRepository = triviaRepo,
            progressRepository = progressRepo,
            reviewRepository = reviewRepo,
            statsRepository = statsRepo,
            settingsRepository = settingsRepo,
            timeProvider = { now + 90_000_000L },
        )

        val state = viewModel.uiState.value
        assertEquals(1, state.dueReviewCount)
    }
}
