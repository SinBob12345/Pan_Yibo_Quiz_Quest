package com.yibopan.quizquest.data.rules

import com.yibopan.quizquest.data.model.Difficulty
import com.yibopan.quizquest.data.model.Question
import com.yibopan.quizquest.data.model.QuestionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/** Unit tests for [QuizEngine] state machine transitions. */
class QuizEngineTest {

    private lateinit var shuffler: AnswerShuffler
    private lateinit var engine: QuizEngine

    private val question1 = Question(
        id = 101L,
        categoryId = 17,
        difficulty = Difficulty.EASY,
        type = QuestionType.MULTIPLE,
        text = "Capital of France?",
        correctAnswer = "Paris",
        incorrectAnswers = listOf("London", "Berlin", "Rome"),
    )

    private val question2 = Question(
        id = 102L,
        categoryId = 17,
        difficulty = Difficulty.EASY,
        type = QuestionType.BOOLEAN,
        text = "Is water liquid at room temp?",
        correctAnswer = "True",
        incorrectAnswers = listOf("False"),
    )

    @Before
    fun setUp() {
        shuffler = AnswerShuffler(Random(42L))
        engine = QuizEngine(shuffler, Random(42L))
    }

    @Test
    fun `start initializes QuizState with options and index 0`() {
        val state = engine.start(listOf(question1, question2))

        assertEquals(2, state.questions.size)
        assertEquals(2, state.options.size)
        assertEquals(0, state.currentIndex)
        assertNull(state.selectedAnswer)
        assertEquals(0, state.correctCount)
        assertEquals(QuizEngine.DEFAULT_HINTS, state.hintsLeft)
        assertTrue(state.removedOptions.isEmpty())
        assertTrue(state.answers.isEmpty())
        assertFalse(state.isAnswered)
        assertFalse(state.isFinished)
    }

    @Test
    fun `selectAnswer updates correctCount and records answer when correct`() {
        val initial = engine.start(listOf(question1))

        val state = engine.selectAnswer(initial, answer = "Paris", timeTakenMs = 1200L)

        assertTrue(state.isAnswered)
        assertEquals("Paris", state.selectedAnswer)
        assertEquals(1, state.correctCount)
        assertEquals(1, state.answers.size)

        val record = state.answers[0]
        assertEquals("Capital of France?", record.questionText)
        assertEquals("Paris", record.chosenAnswer)
        assertEquals("Paris", record.correctAnswer)
        assertTrue(record.isCorrect)
        assertEquals(1200L, record.timeTakenMs)
    }

    @Test
    fun `selectAnswer records answer as wrong when choice is incorrect`() {
        val initial = engine.start(listOf(question1))

        val state = engine.selectAnswer(initial, answer = "London", timeTakenMs = 1500L)

        assertTrue(state.isAnswered)
        assertEquals("London", state.selectedAnswer)
        assertEquals(0, state.correctCount)
        assertEquals(1, state.answers.size)

        val record = state.answers[0]
        assertEquals("London", record.chosenAnswer)
        assertFalse(record.isCorrect)
    }

    @Test
    fun `selectAnswer ignores duplicate calls when already answered`() {
        val initial = engine.start(listOf(question1))
        val answered = engine.selectAnswer(initial, answer = "Paris", timeTakenMs = 1000L)

        val duplicate = engine.selectAnswer(answered, answer = "London", timeTakenMs = 2000L)

        assertEquals("Paris", duplicate.selectedAnswer)
        assertEquals(1, duplicate.correctCount)
        assertEquals(1, duplicate.answers.size)
    }

    @Test
    fun `timeUp records unanswered choice as wrong with null chosenAnswer`() {
        val initial = engine.start(listOf(question1))

        val state = engine.timeUp(initial, timeTakenMs = 5000L)

        assertTrue(state.isAnswered)
        assertEquals(0, state.correctCount)
        assertEquals(1, state.answers.size)

        val record = state.answers[0]
        assertNull(record.chosenAnswer)
        assertFalse(record.isCorrect)
        assertEquals(5000L, record.timeTakenMs)
    }

    @Test
    fun `next advances to next question and clears selectedAnswer and removedOptions`() {
        val initial = engine.start(listOf(question1, question2))
        val answered = engine.selectAnswer(initial, answer = "Paris", timeTakenMs = 1000L)
        val withHint = engine.useFiftyFifty(answered)

        val nextState = engine.next(withHint)

        assertEquals(1, nextState.currentIndex)
        assertNull(nextState.selectedAnswer)
        assertTrue(nextState.removedOptions.isEmpty())
        assertFalse(nextState.isFinished)
    }

    @Test
    fun `next on last question transitions state to isFinished`() {
        val initial = engine.start(listOf(question1))
        val answered = engine.selectAnswer(initial, answer = "Paris", timeTakenMs = 1000L)

        val finishedState = engine.next(answered)

        assertEquals(1, finishedState.currentIndex)
        assertTrue(finishedState.isFinished)
    }

    @Test
    fun `next ignored when current question is unanswered`() {
        val initial = engine.start(listOf(question1, question2))

        val sameState = engine.next(initial)

        assertEquals(0, sameState.currentIndex)
        assertFalse(sameState.isAnswered)
    }

    @Test
    fun `useFiftyFifty removes exactly two wrong options for MULTIPLE question`() {
        val initial = engine.start(listOf(question1))

        val hintState = engine.useFiftyFifty(initial)

        assertEquals(1, hintState.hintsLeft)
        assertEquals(2, hintState.removedOptions.size)
        assertFalse(hintState.removedOptions.contains("Paris"))
    }

    @Test
    fun `useFiftyFifty ignored for BOOLEAN question`() {
        val initial = engine.start(listOf(question2))

        val hintState = engine.useFiftyFifty(initial)

        assertEquals(QuizEngine.DEFAULT_HINTS, hintState.hintsLeft)
        assertTrue(hintState.removedOptions.isEmpty())
    }

    @Test
    fun `useFiftyFifty ignored when no hints left`() {
        val initial = engine.start(listOf(question1)).copy(hintsLeft = 0)

        val hintState = engine.useFiftyFifty(initial)

        assertEquals(0, hintState.hintsLeft)
        assertTrue(hintState.removedOptions.isEmpty())
    }

    @Test
    fun `useFiftyFifty ignored when question is already answered`() {
        val initial = engine.start(listOf(question1))
        val answered = engine.selectAnswer(initial, answer = "Paris", timeTakenMs = 1000L)

        val hintState = engine.useFiftyFifty(answered)

        assertEquals(QuizEngine.DEFAULT_HINTS, hintState.hintsLeft)
        assertTrue(hintState.removedOptions.isEmpty())
    }
}
