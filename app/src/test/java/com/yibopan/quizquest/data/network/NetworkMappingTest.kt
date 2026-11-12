package com.yibopan.quizquest.data.network

import com.yibopan.quizquest.data.model.Category
import com.yibopan.quizquest.data.model.Difficulty
import com.yibopan.quizquest.data.model.Question
import com.yibopan.quizquest.data.model.QuestionType
import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for mapping [NetworkCategory] and [NetworkQuestion] to domain models with text decoding. */
class NetworkMappingTest {

    @Test
    fun `NetworkCategory toCategory decodes URL3986 category name`() {
        val networkCategory = NetworkCategory(id = 9, name = "General%20Knowledge")
        val category = networkCategory.toCategory()

        val expected = Category(id = 9, name = "General Knowledge")
        assertEquals(expected, category)
    }

    @Test
    fun `NetworkCategory toCategory decodes ampersands and symbols`() {
        val networkCategory = NetworkCategory(id = 17, name = "Science%20%26%20Nature")
        val category = networkCategory.toCategory()

        val expected = Category(id = 17, name = "Science & Nature")
        assertEquals(expected, category)
    }

    @Test
    fun `NetworkQuestion toQuestion decodes all text fields and maps enums`() {
        val networkQuestion = NetworkQuestion(
            type = "multiple",
            difficulty = "medium",
            category = "Science%20%26%20Nature",
            question = "What%20is%201%2B1%3F",
            correctAnswer = "2",
            incorrectAnswers = listOf("1", "3", "4"),
        )

        val question = networkQuestion.toQuestion(categoryId = 17, id = 42L)

        val expected = Question(
            id = 42L,
            categoryId = 17,
            difficulty = Difficulty.MEDIUM,
            type = QuestionType.MULTIPLE,
            text = "What is 1+1?",
            correctAnswer = "2",
            incorrectAnswers = listOf("1", "3", "4"),
        )
        assertEquals(expected, question)
    }

    @Test
    fun `NetworkQuestion toQuestion decodes boolean type and easy difficulty`() {
        val networkQuestion = NetworkQuestion(
            type = "boolean",
            difficulty = "easy",
            category = "History",
            question = "Is%20the%20Earth%20round%3F",
            correctAnswer = "True",
            incorrectAnswers = listOf("False"),
        )

        val question = networkQuestion.toQuestion(categoryId = 23)

        assertEquals(Difficulty.EASY, question.difficulty)
        assertEquals(QuestionType.BOOLEAN, question.type)
        assertEquals("Is the Earth round?", question.text)
        assertEquals("True", question.correctAnswer)
        assertEquals(listOf("False"), question.incorrectAnswers)
    }

    @Test
    fun `NetworkQuestion toQuestion handles fallback for unknown difficulty and type`() {
        val networkQuestion = NetworkQuestion(
            type = "unknown_type",
            difficulty = "unknown_difficulty",
            category = "General",
            question = "Sample%20Question",
            correctAnswer = "Ans",
            incorrectAnswers = listOf("Wrong"),
        )

        val question = networkQuestion.toQuestion(categoryId = 9)

        assertEquals(Difficulty.EASY, question.difficulty)
        assertEquals(QuestionType.MULTIPLE, question.type)
        assertEquals("Sample Question", question.text)
    }
}
