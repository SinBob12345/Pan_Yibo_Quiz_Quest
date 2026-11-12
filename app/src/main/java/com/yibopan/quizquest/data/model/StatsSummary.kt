package com.yibopan.quizquest.data.model

/** Answer counts for one category (world). */
data class CategoryAccuracy(
    val categoryId: Int,
    val categoryName: String,
    val answered: Int,
    val correct: Int,
) {
    /** Share of correct answers from 0f to 1f; 0f when nothing was answered yet. */
    val accuracy: Float get() = if (answered == 0) 0f else correct.toFloat() / answered
}

/** Number of questions answered on one calendar day. */
data class DailyActivity(
    val dayStartMillis: Long,
    val questionsAnswered: Int,
)

/**
 * Everything the Stats screen shows. There is deliberately no "streak" —
 * only learning days this week, so missing a day is never punished (A2 commitment #6).
 */
data class StatsSummary(
    val totalAnswered: Int = 0,
    val totalCorrect: Int = 0,
    val totalStars: Int = 0,
    val activeDaysThisWeek: Int = 0,
    val byCategory: List<CategoryAccuracy> = emptyList(),
    val last7Days: List<DailyActivity> = emptyList(),
    val recentResults: List<QuizResult> = emptyList(),
) {
    /** Overall share of correct answers from 0f to 1f. */
    val overallAccuracy: Float get() = if (totalAnswered == 0) 0f else totalCorrect.toFloat() / totalAnswered
}
