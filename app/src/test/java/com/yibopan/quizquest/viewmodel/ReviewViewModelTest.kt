package com.yibopan.quizquest.viewmodel

import com.yibopan.quizquest.data.model.AnswerRecord
import com.yibopan.quizquest.fakes.FakeReviewRepository
import com.yibopan.quizquest.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Unit tests for [ReviewViewModel] flashcard review logic (Lecture 4 / Lecture 7). */
@OptIn(ExperimentalCoroutinesApi::class)
class ReviewViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var reviewRepo: FakeReviewRepository
    private val now = 1_000_000_000L

    @Before
    fun setUp() {
        reviewRepo = FakeReviewRepository()
    }

    @Test
    fun `initialization loads due review cards`() = runTest {
        reviewRepo.addWrongAnswers(
            listOf(AnswerRecord("Wrong Q", "B", "A", false, 1000L)),
            nowMillis = now,
        )

        val viewModel = ReviewViewModel(
            reviewRepository = reviewRepo,
            timeProvider = { now + 90_000_000L },
        )

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.dueCards.size)
        assertEquals("Wrong Q", state.currentCard?.questionText)
        assertFalse(state.isAnswerRevealed)
    }

    @Test
    fun `showAnswer sets isAnswerRevealed to true`() = runTest {
        reviewRepo.addWrongAnswers(
            listOf(AnswerRecord("Wrong Q", "B", "A", false, 1000L)),
            nowMillis = now,
        )

        val viewModel = ReviewViewModel(
            reviewRepository = reviewRepo,
            timeProvider = { now + 90_000_000L },
        )

        viewModel.showAnswer()

        assertTrue(viewModel.uiState.value.isAnswerRevealed)
    }

    @Test
    fun `3 due cards reviews all 3 cards in order and ends in all caught up state`() = runTest {
        reviewRepo.addWrongAnswers(
            listOf(
                AnswerRecord("Card 1", "B", "A", false, 1000L),
                AnswerRecord("Card 2", "B", "A", false, 1000L),
                AnswerRecord("Card 3", "B", "A", false, 1000L),
            ),
            nowMillis = now,
        )

        val viewModel = ReviewViewModel(
            reviewRepository = reviewRepo,
            timeProvider = { now + 90_000_000L },
        )

        // Card 1
        assertEquals("Card 1", viewModel.uiState.value.currentCard?.questionText)
        viewModel.showAnswer()
        viewModel.onKnewIt()

        // Card 2
        assertEquals("Card 2", viewModel.uiState.value.currentCard?.questionText)
        viewModel.showAnswer()
        viewModel.onKnewIt()

        // Card 3
        assertEquals("Card 3", viewModel.uiState.value.currentCard?.questionText)
        viewModel.showAnswer()
        viewModel.onKnewIt()

        // All caught up
        val finalState = viewModel.uiState.value
        assertNull(finalState.currentCard)
        assertTrue(finalState.dueCards.isEmpty())
        assertEquals(3, finalState.reviewedThisSession)
    }

    @Test
    fun `calling onKnewIt twice quickly updates the card only once`() = runTest {
        reviewRepo.addWrongAnswers(
            listOf(AnswerRecord("Wrong Q", "B", "A", false, 1000L)),
            nowMillis = now,
        )

        val viewModel = ReviewViewModel(
            reviewRepository = reviewRepo,
            timeProvider = { now + 90_000_000L },
        )

        viewModel.showAnswer()
        viewModel.onKnewIt()
        viewModel.onKnewIt() // Second rapid call ignored

        assertEquals(1, viewModel.uiState.value.reviewedThisSession)
    }
}
