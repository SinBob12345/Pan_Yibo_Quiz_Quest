package com.yibopan.quizquest.viewmodel

import com.yibopan.quizquest.data.model.Difficulty
import com.yibopan.quizquest.data.model.QuizResult
import com.yibopan.quizquest.fakes.FakeProgressRepository
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

/** Unit tests for [WorldMapViewModel] (Lecture 4 / Lecture 5). */
@OptIn(ExperimentalCoroutinesApi::class)
class WorldMapViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var progressRepository: FakeProgressRepository
    private lateinit var triviaRepository: FakeTriviaRepository

    @Before
    fun setUp() {
        progressRepository = FakeProgressRepository()
        triviaRepository = FakeTriviaRepository()
    }

    @Test
    fun `initial state loads 9 levels with level 1 unlocked and remaining locked`() = runTest {
        val viewModel = WorldMapViewModel(
            categoryId = 17,
            progressRepository = progressRepository,
            triviaRepository = triviaRepository,
        )

        val state = viewModel.uiState.value
        assertEquals(17, state.categoryId)
        assertEquals("Science & Nature", state.worldName)
        assertFalse(state.isLoading)
        assertEquals(9, state.levels.size)

        // Level 1: unlocked, 0 stars
        val level1 = state.levels[0]
        assertEquals(1, level1.level)
        assertEquals(Difficulty.EASY, level1.difficulty)
        assertEquals(0, level1.stars)
        assertTrue(level1.unlocked)

        // Levels 2 to 9: locked
        for (i in 1 until 9) {
            val item = state.levels[i]
            assertEquals(i + 1, item.level)
            assertFalse(item.unlocked)
        }
    }

    @Test
    fun `unlocks level 2 and level 3 when preceding levels earn at least 1 star`() = runTest {
        progressRepository.saveQuizResult(
            QuizResult(0L, 17, 1, 8, 10, 2, 1000L, 10000L),
            emptyList(),
        )
        progressRepository.saveQuizResult(
            QuizResult(0L, 17, 2, 6, 10, 1, 2000L, 10000L),
            emptyList(),
        )

        val viewModel = WorldMapViewModel(
            categoryId = 17,
            progressRepository = progressRepository,
            triviaRepository = triviaRepository,
        )

        val state = viewModel.uiState.value
        val levels = state.levels

        assertTrue(levels[0].unlocked) // Level 1 (2 stars)
        assertEquals(2, levels[0].stars)

        assertTrue(levels[1].unlocked) // Level 2 (1 star)
        assertEquals(1, levels[1].stars)

        assertTrue(levels[2].unlocked) // Level 3 (unlocked, 0 stars)
        assertEquals(0, levels[2].stars)

        assertFalse(levels[3].unlocked) // Level 4 (locked)
    }

    @Test
    fun `does not unlock level 3 when level 2 has 0 stars`() = runTest {
        progressRepository.saveQuizResult(
            QuizResult(0L, 17, 1, 8, 10, 2, 1000L, 10000L),
            emptyList(),
        )
        progressRepository.saveQuizResult(
            QuizResult(0L, 17, 2, 4, 10, 0, 2000L, 10000L),
            emptyList(),
        )

        val viewModel = WorldMapViewModel(
            categoryId = 17,
            progressRepository = progressRepository,
            triviaRepository = triviaRepository,
        )

        val state = viewModel.uiState.value
        val levels = state.levels

        assertTrue(levels[0].unlocked) // Level 1
        assertTrue(levels[1].unlocked) // Level 2
        assertEquals(0, levels[1].stars)

        assertFalse(levels[2].unlocked) // Level 3 locked
    }
}
