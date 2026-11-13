package com.yibopan.quizquest.data.rules

import com.yibopan.quizquest.data.model.Difficulty
import com.yibopan.quizquest.data.model.Question
import com.yibopan.quizquest.data.model.QuestionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Unit tests for [AnswerShuffler] option shuffling. */
class AnswerShufflerTest {

    @Test
    fun `optionsFor MULTIPLE question contains all options shuffled with random`() {
        val seededRandom = Random(42L)
        val shuffler = AnswerShuffler(seededRandom)

        val question = Question(
            id = 1L,
            categoryId = 17,
            difficulty = Difficulty.EASY,
            type = QuestionType.MULTIPLE,
            text = "What is 1+1?",
            correctAnswer = "2",
            incorrectAnswers = listOf("1", "3", "4"),
        )

        val options = shuffler.optionsFor(question)

        assertEquals(4, options.size)
        assertTrue(options.containsAll(listOf("2", "1", "3", "4")))
    }

    @Test
    fun `optionsFor BOOLEAN question returns fixed True and False list`() {
        val shuffler = AnswerShuffler(Random(123L))

        val question = Question(
            id = 2L,
            categoryId = 23,
            difficulty = Difficulty.EASY,
            type = QuestionType.BOOLEAN,
            text = "Is the Earth round?",
            correctAnswer = "True",
            incorrectAnswers = listOf("False"),
        )

        val options = shuffler.optionsFor(question)

        assertEquals(listOf("True", "False"), options)
    }
}
