package com.yibopan.quizquest.viewmodel

import com.yibopan.quizquest.data.model.DailyActivity
import com.yibopan.quizquest.data.model.StatsSummary
import com.yibopan.quizquest.data.repository.StatsRepository
import com.yibopan.quizquest.fakes.FakeStatsRepository
import com.yibopan.quizquest.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Unit tests for [StatsViewModel] (Lecture 4 / Lecture 7). */
@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var statsRepo: FakeStatsRepository
    private lateinit var viewModel: StatsViewModel

    @Before
    fun setUp() {
        statsRepo = FakeStatsRepository()
        viewModel = StatsViewModel(statsRepo)
    }

    @Test
    fun `initial state transitions from loading to loaded state`() = runTest {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNotNull(state.summary)
    }

    @Test
    fun `empty history shows zeros in summary`() = runTest {
        val summary = viewModel.uiState.value.summary
        assertEquals(0, summary.totalAnswered)
        assertEquals(0, summary.totalCorrect)
        assertEquals(0, summary.totalStars)
        assertEquals(0, summary.activeDaysThisWeek)
        assertTrue(summary.last7Days.isEmpty())
        assertTrue(summary.recentResults.isEmpty())
    }

    @Test
    fun `activeDaysThisWeek and last7Days are passed through unchanged`() = runTest {
        val dailyActivities = listOf(
            DailyActivity(dayStartMillis = 1000L, questionsAnswered = 5),
            DailyActivity(dayStartMillis = 2000L, questionsAnswered = 10),
        )
        val expectedSummary = StatsSummary(
            totalAnswered = 15,
            totalCorrect = 12,
            totalStars = 4,
            activeDaysThisWeek = 3,
            last7Days = dailyActivities,
        )

        statsRepo.statsSummaryFlow.value = expectedSummary

        val summary = viewModel.uiState.value.summary
        assertEquals(3, summary.activeDaysThisWeek)
        assertEquals(2, summary.last7Days.size)
        assertEquals(5, summary.last7Days[0].questionsAnswered)
        assertEquals(10, summary.last7Days[1].questionsAnswered)
    }

    @Test
    fun `catches repository error and sets error in uiState`() = runTest {
        val failingRepo = object : StatsRepository {
            override suspend fun getStatsSummary(): Flow<StatsSummary> = flow {
                throw IllegalStateException("Failed to load stats summary")
            }
        }

        val errorViewModel = StatsViewModel(failingRepo)

        val state = errorViewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertEquals("Failed to load statistics.", state.error)
    }
}
