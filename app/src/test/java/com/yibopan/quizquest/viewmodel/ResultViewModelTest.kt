package com.yibopan.quizquest.viewmodel

import com.yibopan.quizquest.data.model.QuizResult
import com.yibopan.quizquest.data.model.UserSettings
import com.yibopan.quizquest.fakes.FakeProgressRepository
import com.yibopan.quizquest.fakes.FakeSettingsRepository
import com.yibopan.quizquest.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Unit tests for [ResultViewModel] (Lecture 4 / Lecture 5). */
@OptIn(ExperimentalCoroutinesApi::class)
class ResultViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var progressRepo: FakeProgressRepository
    private lateinit var settingsRepo: FakeSettingsRepository

    @Before
    fun setUp() {
        progressRepo = FakeProgressRepository()
        settingsRepo = FakeSettingsRepository()
    }

    @Test
    fun `initialization loads session result and maps score and stars`() = runTest {
        val result = QuizResult(
            sessionId = 0L,
            categoryId = 17,
            level = 1,
            correctCount = 8,
            totalCount = 10,
            stars = 2,
            startedAt = 1000L,
            durationMs = 15000L,
        )
        val sessionId = progressRepo.saveQuizResult(result, emptyList())

        val viewModel = ResultViewModel(
            sessionId = sessionId,
            categoryId = 17,
            level = 1,
            progressRepository = progressRepo,
            settingsRepository = settingsRepo,
            timeProvider = { 1000L },
        )

        val state = viewModel.uiState.value
        assertEquals(sessionId, state.sessionId)
        assertEquals(17, state.categoryId)
        assertEquals(1, state.level)
        assertEquals(8, state.correctCount)
        assertEquals(10, state.totalCount)
        assertEquals(2, state.stars)
        assertTrue(state.isNextLevelUnlocked)
    }

    @Test
    fun `isNextLevelUnlocked is false when completing final level 9`() = runTest {
        val result = QuizResult(
            sessionId = 0L,
            categoryId = 17,
            level = 9,
            correctCount = 10,
            totalCount = 10,
            stars = 3,
            startedAt = 1000L,
            durationMs = 15000L,
        )
        val sessionId = progressRepo.saveQuizResult(result, emptyList())

        val viewModel = ResultViewModel(
            sessionId = sessionId,
            categoryId = 17,
            level = 9,
            progressRepository = progressRepo,
            settingsRepository = settingsRepo,
            timeProvider = { 1000L },
        )

        val state = viewModel.uiState.value
        assertFalse(state.isNextLevelUnlocked)
    }

    @Test
    fun `detects when daily goal is reached`() = runTest {
        settingsRepo.updateSettings(UserSettings(dailyGoalLevels = 2))

        val result1 = QuizResult(0L, 17, 1, 8, 10, 2, 1000L, 10000L)
        val result2 = QuizResult(0L, 17, 2, 9, 10, 2, 2000L, 10000L)

        progressRepo.saveQuizResult(result1, emptyList())
        val sessionId2 = progressRepo.saveQuizResult(result2, emptyList())

        val viewModel = ResultViewModel(
            sessionId = sessionId2,
            categoryId = 17,
            level = 2,
            progressRepository = progressRepo,
            settingsRepository = settingsRepo,
            timeProvider = { 3000L },
        )

        val state = viewModel.uiState.value
        assertTrue(state.isDailyGoalReached)
    }

    @Test
    fun `isDailyGoalReached is false when goal not yet reached`() = runTest {
        settingsRepo.updateSettings(UserSettings(dailyGoalLevels = 3))

        val result1 = QuizResult(0L, 17, 1, 8, 10, 2, 1000L, 10000L)
        val sessionId1 = progressRepo.saveQuizResult(result1, emptyList())

        val viewModel = ResultViewModel(
            sessionId = sessionId1,
            categoryId = 17,
            level = 1,
            progressRepository = progressRepo,
            settingsRepository = settingsRepo,
            timeProvider = { 2000L },
        )

        val state = viewModel.uiState.value
        assertFalse(state.isDailyGoalReached)
    }
}
