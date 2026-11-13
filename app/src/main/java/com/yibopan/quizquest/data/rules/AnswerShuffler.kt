package com.yibopan.quizquest.data.rules

import com.yibopan.quizquest.data.model.Question
import com.yibopan.quizquest.data.model.QuestionType
import kotlin.random.Random

/**
 * Stage 3 — builds the list of answer options shown for a question.
 *
 *  - MULTIPLE: correct + incorrect answers shuffled with [random] (tests pass a seeded
 *    Random so the order is predictable).
 *  - BOOLEAN: always the fixed order "True", "False" (never shuffled).
 */
class AnswerShuffler(private val random: Random = Random.Default) {

    fun optionsFor(question: Question): List<String> = when (question.type) {
        QuestionType.MULTIPLE -> (listOf(question.correctAnswer) + question.incorrectAnswers).shuffled(random)
        QuestionType.BOOLEAN -> listOf("True", "False")
    }
}
