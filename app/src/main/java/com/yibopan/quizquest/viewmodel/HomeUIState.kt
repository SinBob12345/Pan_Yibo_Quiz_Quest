package com.yibopan.quizquest.viewmodel

import com.yibopan.quizquest.data.model.Category
import com.yibopan.quizquest.data.model.UserSettings

/** Everything the Home screen shows (Lecture 5: a UI state data class). */
data class HomeUIState(
    val isLoading: Boolean = false,
    val worlds: List<Category> = emptyList(),
    val error: String? = null,
    val questionsAnsweredToday: Int = 0,
    val levelsCompletedToday: Int = 0,
    val dailyGoal: Int = UserSettings().dailyGoalLevels,
    /** Shows the gentle "done for today" stopping cue (A2 commitment #6). */
    val isDailyGoalReached: Boolean = false,
    val dueReviewCount: Int = 0,
    /** Learning days this week — deliberately not a streak. */
    val activeDaysThisWeek: Int = 0,
)
