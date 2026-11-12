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
        QuestionType.MULTIPLE -> TODO("Stage 3: shuffle with random")
        QuestionType.BOOLEAN -> TODO("Stage 3: return listOf(\"True\", \"False\")")
    }
}
