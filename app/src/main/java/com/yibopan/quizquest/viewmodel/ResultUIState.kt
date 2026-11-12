package com.yibopan.quizquest.viewmodel

/** Everything the Result screen shows. */
data class ResultUIState(
    val sessionId: Long,
    val categoryId: Int = 0,
    val level: Int = 0,
    val correctCount: Int = 0,
    val totalCount: Int = 0,
    val stars: Int = 0,
    val isNextLevelUnlocked: Boolean = false,
    /** When true the screen also shows the "done for today" card. */
    val isDailyGoalReached: Boolean = false,
)
