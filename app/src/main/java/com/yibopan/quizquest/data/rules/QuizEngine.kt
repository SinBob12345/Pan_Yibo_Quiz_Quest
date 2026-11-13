package com.yibopan.quizquest.data.rules

import com.yibopan.quizquest.data.model.AnswerRecord
import com.yibopan.quizquest.data.model.Question
import com.yibopan.quizquest.data.model.QuestionType
import kotlin.random.Random

/**
 * Immutable state of one quiz session (Lecture 2: UI state is represented by immutable data
 * objects; we create a new copy instead of changing it). QuizViewModel keeps one of these.
 */
data class QuizState(
    val questions: List<Question> = emptyList(),
    /** Options per question, already shuffled by AnswerShuffler (same index as [questions]). */
    val options: List<List<String>> = emptyList(),
    val currentIndex: Int = 0,
    /** The learner's choice for the current question; null while unanswered. */
    val selectedAnswer: String? = null,
    val correctCount: Int = 0,
    val hintsLeft: Int = QuizEngine.DEFAULT_HINTS,
    /** Options hidden by the 50/50 hint for the current question. */
    val removedOptions: Set<String> = emptySet(),
    val answers: List<AnswerRecord> = emptyList(),
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentIndex)
    val isAnswered: Boolean get() = selectedAnswer != null
    val isFinished: Boolean get() = questions.isNotEmpty() && currentIndex >= questions.size
}

/**
 * Stage 3 — pure state machine for a quiz session (plain Kotlin, no Android).
 *
 *  - start: builds options with [shuffler]; currentIndex = 0; hintsLeft = DEFAULT_HINTS.
 *  - selectAnswer: ignored if already answered or finished; records an AnswerRecord and
 *    increments correctCount when correct.
 *  - timeUp: like selectAnswer with no choice (chosenAnswer = null, counts as wrong).
 *  - next: only allowed after answering; moves to the next question and clears
 *    selectedAnswer and removedOptions. After the last question the state isFinished.
 *  - useFiftyFifty: only for MULTIPLE questions that are unanswered and when hintsLeft > 0;
 *    removes exactly two wrong options chosen with [random], hintsLeft - 1.
 *    Otherwise returns the state unchanged.
 */
class QuizEngine(
    private val shuffler: AnswerShuffler,
    private val random: Random = Random.Default,
) {

    fun start(questions: List<Question>): QuizState {
        val optionsList = questions.map { shuffler.optionsFor(it) }
        return QuizState(
            questions = questions,
            options = optionsList,
            currentIndex = 0,
            selectedAnswer = null,
            correctCount = 0,
            hintsLeft = DEFAULT_HINTS,
            removedOptions = emptySet(),
            answers = emptyList(),
        )
    }

    fun selectAnswer(state: QuizState, answer: String, timeTakenMs: Long): QuizState {
        val currentQ = state.currentQuestion ?: return state
        if (state.isAnswered || state.isFinished) return state

        val isCorrect = (answer == currentQ.correctAnswer)
        val record = AnswerRecord(
            questionText = currentQ.text,
            chosenAnswer = answer,
            correctAnswer = currentQ.correctAnswer,
            isCorrect = isCorrect,
            timeTakenMs = timeTakenMs,
        )

        return state.copy(
            selectedAnswer = answer,
            correctCount = if (isCorrect) state.correctCount + 1 else state.correctCount,
            answers = state.answers + record,
        )
    }

    fun timeUp(state: QuizState, timeTakenMs: Long): QuizState {
        val currentQ = state.currentQuestion ?: return state
        if (state.isAnswered || state.isFinished) return state

        val record = AnswerRecord(
            questionText = currentQ.text,
            chosenAnswer = null,
            correctAnswer = currentQ.correctAnswer,
            isCorrect = false,
            timeTakenMs = timeTakenMs,
        )

        return state.copy(
            selectedAnswer = TIME_UP_SELECTION,
            answers = state.answers + record,
        )
    }

    fun next(state: QuizState): QuizState {
        if (!state.isAnswered || state.isFinished) return state
        return state.copy(
            currentIndex = state.currentIndex + 1,
            selectedAnswer = null,
            removedOptions = emptySet(),
        )
    }

    fun useFiftyFifty(state: QuizState): QuizState {
        val currentQ = state.currentQuestion ?: return state
        if (currentQ.type != QuestionType.MULTIPLE ||
            state.isAnswered ||
            state.isFinished ||
            state.hintsLeft <= 0 ||
            state.removedOptions.isNotEmpty()
        ) {
            return state
        }

        val wrongToHide = currentQ.incorrectAnswers.shuffled(random).take(2).toSet()
        return state.copy(
            hintsLeft = state.hintsLeft - 1,
            removedOptions = wrongToHide,
        )
    }

    companion object {
        const val DEFAULT_HINTS = 2
        private const val TIME_UP_SELECTION = ""
    }
}
