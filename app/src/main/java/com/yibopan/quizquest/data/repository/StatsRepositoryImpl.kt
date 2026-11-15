package com.yibopan.quizquest.data.repository

import com.yibopan.quizquest.data.database.CategoryDao
import com.yibopan.quizquest.data.database.QuizSessionDao
import com.yibopan.quizquest.data.database.toCategory
import com.yibopan.quizquest.data.database.toQuizResult
import com.yibopan.quizquest.data.model.CategoryAccuracy
import com.yibopan.quizquest.data.model.DailyActivity
import com.yibopan.quizquest.data.model.StatsSummary
import com.yibopan.quizquest.data.rules.CategoryFilter
import com.yibopan.quizquest.data.rules.DailyGoalTracker
import com.yibopan.quizquest.data.rules.SystemTimeProvider
import com.yibopan.quizquest.data.rules.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.TimeZone

/**
 * Builds read-only learning statistics from Room queries (Lecture 7).
 *
 * Uses [DailyGoalTracker.activeDaysThisWeek] — strictly no streaks (A2 commitment #6).
 */
class StatsRepositoryImpl(
    private val quizSessionDao: QuizSessionDao,
    private val categoryDao: CategoryDao,
    private val timeProvider: TimeProvider = SystemTimeProvider,
) : StatsRepository {

    override suspend fun getStatsSummary(): Flow<StatsSummary> {
        val timeZone = TimeZone.getDefault()
        return combine(
            quizSessionDao.getAllSessions(),
            categoryDao.getCategories(),
        ) { sessions, categoryEntities ->
            val now = timeProvider.now()
            val categories = CategoryFilter.filterAllowed(categoryEntities.map { it.toCategory() })

            val totalAnswered = sessions.sumOf { it.totalCount }
            val totalCorrect = sessions.sumOf { it.correctCount }
            val totalStars = sessions.sumOf { it.stars }

            val sessionTimes = sessions.map { it.startedAt }
            val activeDays = DailyGoalTracker.activeDaysThisWeek(sessionTimes, now, timeZone)

            val byCategory = categories.map { category ->
                val catSessions = sessions.filter { it.categoryId == category.id }
                CategoryAccuracy(
                    categoryId = category.id,
                    categoryName = category.name,
                    answered = catSessions.sumOf { it.totalCount },
                    correct = catSessions.sumOf { it.correctCount },
                )
            }

            val last7Days = (6 downTo 0).map { daysAgo ->
                val dayMillis = now - (daysAgo * 24L * 60L * 60L * 1000L)
                val dayStart = DailyGoalTracker.startOfDayMillis(dayMillis, timeZone)
                val dayEnd = dayStart + (24L * 60L * 60L * 1000L)
                val count = sessions
                    .filter { it.startedAt in dayStart until dayEnd }
                    .sumOf { it.totalCount }
                DailyActivity(dayStartMillis = dayStart, questionsAnswered = count)
            }

            val recentResults = sessions.take(10).map { it.toQuizResult() }

            StatsSummary(
                totalAnswered = totalAnswered,
                totalCorrect = totalCorrect,
                totalStars = totalStars,
                activeDaysThisWeek = activeDays,
                byCategory = byCategory,
                last7Days = last7Days,
                recentResults = recentResults,
            )
        }
    }
}
