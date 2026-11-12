package com.yibopan.quizquest.data.model

/**
 * Learner preferences, saved in Room (Lecture 7) in Stage 4.
 * The defaults are deliberate ethical choices (A2 commitments): no timer pressure,
 * a modest daily goal, and answer history kept for 90 days only.
 */
data class UserSettings(
    val questionsPerLevel: Int = 10,
    val timerMode: TimerMode = TimerMode.OFF,
    val soundEnabled: Boolean = true,
    val reduceMotion: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dailyGoalLevels: Int = 3,
    val answerRetention: RetentionPeriod = RetentionPeriod.DAYS_90,
)

/** Optional per-question timer. OFF is the default so learners are not pressured. */
enum class TimerMode(val secondsPerQuestion: Int?) {
    OFF(null),
    RELAXED(30),
    CHALLENGE(15),
}

/** App colour theme. */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

/** How long detailed answer records are kept before they are deleted. */
enum class RetentionPeriod(val days: Int?) {
    DAYS_30(30),
    DAYS_90(90),
    FOREVER(null),
}
