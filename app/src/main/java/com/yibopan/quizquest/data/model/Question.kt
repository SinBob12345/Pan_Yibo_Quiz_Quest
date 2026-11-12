package com.yibopan.quizquest.data.model

/** Question difficulty, using the exact values the Open Trivia DB API expects (Lecture 1: enum class). */
enum class Difficulty(val apiValue: String) {
    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard"),
    ;

    companion object {
        /** Maps an API value to a [Difficulty]; unknown values fall back to [EASY]. */
        fun fromApi(value: String): Difficulty = entries.firstOrNull { it.apiValue == value } ?: EASY
    }
}

/** Question format, using the exact values the Open Trivia DB API expects. */
enum class QuestionType(val apiValue: String) {
    MULTIPLE("multiple"),
    BOOLEAN("boolean"),
    ;

    companion object {
        /** Maps an API value to a [QuestionType]; unknown values fall back to [MULTIPLE]. */
        fun fromApi(value: String): QuestionType = entries.firstOrNull { it.apiValue == value } ?: MULTIPLE
    }
}

/**
 * A decoded, ready-to-show question. Text is already URL-decoded
 * (see data/network/TextDecoder) so the UI never sees encoded characters.
 */
data class Question(
    val id: Long,
    val categoryId: Int,
    val difficulty: Difficulty,
    val type: QuestionType,
    val text: String,
    val correctAnswer: String,
    val incorrectAnswers: List<String>,
)
