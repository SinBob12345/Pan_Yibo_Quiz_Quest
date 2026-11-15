package com.yibopan.quizquest.data.repository

import com.yibopan.quizquest.data.database.CategoryEntity
import com.yibopan.quizquest.data.database.QuizSessionEntity
import com.yibopan.quizquest.fakes.FakeCategoryDao
import com.yibopan.quizquest.fakes.FakeQuizSessionDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** Unit tests for [StatsRepositoryImpl] (Lecture 7). */
@OptIn(ExperimentalCoroutinesApi::class)
class StatsRepositoryTest {

    private lateinit var sessionDao: FakeQuizSessionDao
    private lateinit var categoryDao: FakeCategoryDao
    private lateinit var repository: StatsRepositoryImpl

    @Before
    fun setUp() {
        sessionDao = FakeQuizSessionDao()
        categoryDao = FakeCategoryDao()
        repository = StatsRepositoryImpl(
            quizSessionDao = sessionDao,
            categoryDao = categoryDao,
            timeProvider = { 1_000_000L },
        )
    }

    @Test
    fun `getStatsSummary aggregates total counts stars and accuracy`() = runTest {
        categoryDao.insertAll(
            listOf(
                CategoryEntity(9, "General Knowledge", 1000L),
                CategoryEntity(17, "Science & Nature", 1000L),
            ),
        )

        sessionDao.insert(
            QuizSessionEntity(id = 0L, categoryId = 17, level = 1, startedAt = 1000L, durationMs = 10000L, correctCount = 8, totalCount = 10, stars = 2),
        )
        sessionDao.insert(
            QuizSessionEntity(id = 0L, categoryId = 17, level = 2, startedAt = 2000L, durationMs = 10000L, correctCount = 10, totalCount = 10, stars = 3),
        )

        val summary = repository.getStatsSummary().first()

        assertEquals(20, summary.totalAnswered)
        assertEquals(18, summary.totalCorrect)
        assertEquals(5, summary.totalStars)
        assertEquals(2, summary.recentResults.size)
    }
}
