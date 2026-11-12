package com.yibopan.quizquest.viewmodel

/** Everything the Quiz screen shows. Built from QuizState (data/rules) in Stage 5. */
data class QuizUIState(
    val categoryId: Int,
    val level: Int,
    val isLoading: Boolean = false,
    /** Technical error text (for Logcat only); the screen shows a friendly message instead. */
    val error: String? = null,
    /** 1-based number of the current question. */
    val questionNumber: Int = 0,
    val totalQuestions: Int = 0,
    val questionText: String = "",
    val options: List<String> = emptyList(),
    /** Options hidden by the 50/50 hint. */
    val removedOptions: Set<String> = emptySet(),
    val selectedAnswer: String? = null,
    /** Revealed only after the learner answers, to show feedback. */
    val correctAnswer: String? = null,
    val hintsLeft: Int = 0,
    /** Null when the timer is OFF (the default). */
    val secondsLeft: Int? = null,
    /** Set when the session is saved; the screen then navigates to the Result screen once. */
    val finishedSessionId: Long? = null,
)
