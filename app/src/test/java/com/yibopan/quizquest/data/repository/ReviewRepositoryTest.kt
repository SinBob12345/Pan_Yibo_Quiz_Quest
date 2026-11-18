package com.yibopan.quizquest.data.repository

import com.yibopan.quizquest.data.database.ReviewCardEntity
import com.yibopan.quizquest.data.model.AnswerRecord
import com.yibopan.quizquest.data.model.ReviewCard
import com.yibopan.quizquest.fakes.FakeReviewCardDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Unit tests for [ReviewRepositoryImpl] (Lecture 7). */
@OptIn(ExperimentalCoroutinesApi::class)
class ReviewRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var reviewCardDao: FakeReviewCardDao
    private lateinit var repository: ReviewRepositoryImpl
    private val now = 1_000_000_000L

    @Before
    fun setUp() {
        reviewCardDao = FakeReviewCardDao()
        repository = ReviewRepositoryImpl(
            reviewCardDao = reviewCardDao,
            dispatcher = testDispatcher,
        )
    }

    @Test
    fun `addWrongAnswers creates new review cards in box 1`() = runTest {
        val answers = listOf(
            AnswerRecord("Q1", "Wrong", "Right", false, 1000L),
            AnswerRecord("Q2", "Right", "Right", true, 1000L),
        )

        repository.addWrongAnswers(answers, nowMillis = now)

        val dueCount = repository.getDueCount(nowMillis = now + 90_000_000L).first()
        assertEquals(1, dueCount)

        val dueCards = repository.getDueCards(nowMillis = now + 90_000_000L).first()
        assertEquals("Q1", dueCards[0].questionText)
        assertEquals(1, dueCards[0].box)
    }

    @Test
    fun `updateCard advances box when correct`() = runTest {
        val card = ReviewCard(id = 1L, questionText = "Q1", correctAnswer = "A", box = 1, nextReviewAt = now, timesWrong = 1)
        reviewCardDao.insert(ReviewCardEntity(1L, "Q1", "A", 1, now, 1, now))

        repository.updateCard(card, correct = true, nowMillis = now)

        val dueCards = repository.getDueCards(nowMillis = now).first()
        assertTrue(dueCards.isEmpty())
    }

    @Test
    fun `updateCard resets box to 1 when wrong`() = runTest {
        val card = ReviewCard(id = 1L, questionText = "Q1", correctAnswer = "A", box = 3, nextReviewAt = now, timesWrong = 1)
        reviewCardDao.insert(ReviewCardEntity(1L, "Q1", "A", 3, now, 1, now))

        repository.updateCard(card, correct = false, nowMillis = now)

        val dueCards = repository.getDueCards(nowMillis = now + 90_000_000L).first()
        assertEquals(1, dueCards.size)
        assertEquals(1, dueCards[0].box)
        assertEquals(2, dueCards[0].timesWrong)
    }

    @Test
    fun `updateCard keeps original createdAt`() = runTest {
        val initialCreatedAt = 500_000_000L
        reviewCardDao.insert(ReviewCardEntity(1L, "Q1", "A", 1, now, 1, initialCreatedAt))

        val card = ReviewCard(id = 1L, questionText = "Q1", correctAnswer = "A", box = 1, nextReviewAt = now, timesWrong = 1)

        val updateTime = 1_000_000_000L
        repository.updateCard(card, correct = true, nowMillis = updateTime)

        val updatedEntity = reviewCardDao.getById(1L)
        assertEquals(initialCreatedAt, updatedEntity?.createdAt)
    }
}
