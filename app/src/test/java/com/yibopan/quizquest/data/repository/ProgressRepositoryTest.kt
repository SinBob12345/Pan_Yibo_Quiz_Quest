package com.yibopan.quizquest.data.repository

import com.yibopan.quizquest.data.model.AnswerRecord
import com.yibopan.quizquest.data.model.QuizResult
import com.yibopan.quizquest.fakes.FakeAnswerRecordDao
import com.yibopan.quizquest.fakes.FakeLevelProgressDao
import com.yibopan.quizquest.fakes.FakeQuizSessionDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/** Unit tests for [ProgressRepositoryImpl] (Lecture 7). */
@OptIn(ExperimentalCoroutinesApi::class)
class ProgressRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var progressDao: FakeLevelProgressDao
    private lateinit var sessionDao: FakeQuizSessionDao
    private lateinit var answerDao: FakeAnswerRecordDao
    private lateinit var repository: ProgressRepositoryImpl

    @Before
    fun setUp() {
        progressDao = FakeLevelProgressDao()
        sessionDao = FakeQuizSessionDao()
        answerDao = FakeAnswerRecordDao()
        repository = ProgressRepositoryImpl(
            levelProgressDao = progressDao,
            quizSessionDao = sessionDao,
            answerRecordDao = answerDao,
            timeProvider = { 1000L },
            dispatcher = testDispatcher,
        )
    }

    @Test
    fun `saveQuizResult keeps best stars and score`() = runTest {
        val result1 = QuizResult(
            sessionId = 0L,
            categoryId = 17,
            level = 1,
            correctCount = 6,
            totalCount = 10,
            stars = 1,
            startedAt = 1000L,
            durationMs = 20000L,
        )
        val answers1 = listOf(
            AnswerRecord("Q1", "A", "A", true, 1000L),
        )

        val sessionId1 = repository.saveQuizResult(result1, answers1)
        assertEquals(1L, sessionId1)

        var progressList = repository.getLevelProgress(17).first()
        assertEquals(1, progressList.size)
        assertEquals(1, progressList[0].stars)
        assertEquals(6, progressList[0].bestScore)

        val result2 = QuizResult(
            sessionId = 0L,
            categoryId = 17,
            level = 1,
            correctCount = 9,
            totalCount = 10,
            stars = 2,
            startedAt = 2000L,
            durationMs = 20000L,
        )
        repository.saveQuizResult(result2, emptyList())

        progressList = repository.getLevelProgress(17).first()
        assertEquals(2, progressList[0].stars)
        assertEquals(9, progressList[0].bestScore)

        val result3 = QuizResult(
            sessionId = 0L,
            categoryId = 17,
            level = 1,
            correctCount = 4,
            totalCount = 10,
            stars = 0,
            startedAt = 3000L,
            durationMs = 20000L,
        )
        repository.saveQuizResult(result3, emptyList())

        progressList = repository.getLevelProgress(17).first()
        assertEquals(2, progressList[0].stars)
        assertEquals(9, progressList[0].bestScore)
    }

    @Test
    fun `getQuizResult returns saved quiz result`() = runTest {
        val result = QuizResult(
            sessionId = 0L,
            categoryId = 17,
            level = 1,
            correctCount = 10,
            totalCount = 10,
            stars = 3,
            startedAt = 5000L,
            durationMs = 15000L,
        )
        val sessionId = repository.saveQuizResult(result, emptyList())

        val loaded = repository.getQuizResult(sessionId).first()
        assertNotNull(loaded)
        assertEquals(17, loaded?.categoryId)
        assertEquals(3, loaded?.stars)
    }

    @Test
    fun `getLevelsCompletedSince returns count of passed levels`() = runTest {
        val passed = QuizResult(sessionId = 0L, categoryId = 17, level = 1, correctCount = 8, totalCount = 10, stars = 2, startedAt = 5000L, durationMs = 10000L)
        val failed = QuizResult(sessionId = 0L, categoryId = 17, level = 2, correctCount = 4, totalCount = 10, stars = 0, startedAt = 6000L, durationMs = 10000L)

        repository.saveQuizResult(passed, emptyList())
        repository.saveQuizResult(failed, emptyList())

        val count = repository.getLevelsCompletedSince(startMillis = 4000L).first()
        assertEquals(1, count)
    }
}
