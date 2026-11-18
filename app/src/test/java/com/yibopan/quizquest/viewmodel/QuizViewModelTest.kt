package com.yibopan.quizquest.viewmodel

import com.yibopan.quizquest.data.model.Difficulty
import com.yibopan.quizquest.data.model.Question
import com.yibopan.quizquest.data.model.QuestionType
import com.yibopan.quizquest.data.model.TimerMode
import com.yibopan.quizquest.data.model.UserSettings
import com.yibopan.quizquest.data.network.NetworkResult
import com.yibopan.quizquest.data.rules.AnswerShuffler
import com.yibopan.quizquest.data.rules.QuizEngine
import com.yibopan.quizquest.fakes.FakeProgressRepository
import com.yibopan.quizquest.fakes.FakeReviewRepository
import com.yibopan.quizquest.fakes.FakeSettingsRepository
import com.yibopan.quizquest.fakes.FakeSoundPlayer
import com.yibopan.quizquest.fakes.FakeTriviaRepository
import com.yibopan.quizquest.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

/** Unit tests for [QuizViewModel] including timer, question reporting, sound, and session saving rules (Lecture 5). */
@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var triviaRepo: FakeTriviaRepository
    private lateinit var progressRepo: FakeProgressRepository
    private lateinit var reviewRepo: FakeReviewRepository
    private lateinit var settingsRepo: FakeSettingsRepository
    private lateinit var quizEngine: QuizEngine

    private val question1 = Question(
        id = 1L,
        categoryId = 17,
        difficulty = Difficulty.EASY,
        type = QuestionType.MULTIPLE,
        text = "Capital of France?",
        correctAnswer = "Paris",
        incorrectAnswers = listOf("London", "Berlin", "Rome"),
    )

    private val question2 = Question(
        id = 2L,
        categoryId = 17,
        difficulty = Difficulty.EASY,
        type = QuestionType.MULTIPLE,
        text = "Capital of Japan?",
        correctAnswer = "Tokyo",
        incorrectAnswers = listOf("Kyoto", "Osaka", "Nagoya"),
    )

    @Before
    fun setUp() {
        triviaRepo = FakeTriviaRepository()
        progressRepo = FakeProgressRepository()
        reviewRepo = FakeReviewRepository()
        settingsRepo = FakeSettingsRepository()
        quizEngine = QuizEngine(AnswerShuffler(Random(42L)), Random(42L))

        triviaRepo.questionsResult = NetworkResult.Success(listOf(question1, question2))
    }

    @Test
    fun `initialization loads questions and maps state`() = runTest {
        val viewModel = QuizViewModel(
            categoryId = 17,
            level = 1,
            triviaRepository = triviaRepo,
            progressRepository = progressRepo,
            reviewRepository = reviewRepo,
            settingsRepository = settingsRepo,
            quizEngine = quizEngine,
            timeProvider = { 1000L },
        )

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(1, state.questionNumber)
        assertEquals(2, state.totalQuestions)
        assertEquals("Capital of France?", state.questionText)
        assertEquals(4, state.options.size)
        assertNull(state.selectedAnswer)
        assertNull(state.correctAnswer)
        assertNull(state.secondsLeft)
    }

    @Test
    fun `selectAnswer updates UI state with chosen answer and reveals correctAnswer`() = runTest {
        val viewModel = QuizViewModel(
            categoryId = 17,
            level = 1,
            triviaRepository = triviaRepo,
            progressRepository = progressRepo,
            reviewRepository = reviewRepo,
            settingsRepository = settingsRepo,
            quizEngine = quizEngine,
            timeProvider = { 1000L },
        )

        viewModel.selectAnswer("Paris")

        val state = viewModel.uiState.value
        assertEquals("Paris", state.selectedAnswer)
        assertEquals("Paris", state.correctAnswer)
    }

    @Test
    fun `soundPlayer plays correct or wrong sound when soundEnabled is true`() = runTest {
        val soundPlayer = FakeSoundPlayer()
        settingsRepo.updateSettings(UserSettings(soundEnabled = true))

        val viewModel = QuizViewModel(
            categoryId = 17,
            level = 1,
            triviaRepository = triviaRepo,
            progressRepository = progressRepo,
            reviewRepository = reviewRepo,
            settingsRepository = settingsRepo,
            quizEngine = quizEngine,
            soundPlayer = soundPlayer,
            timeProvider = { 1000L },
        )

        viewModel.selectAnswer("Paris")
        assertEquals(1, soundPlayer.playedCorrectCount)
        assertEquals(0, soundPlayer.playedWrongCount)

        viewModel.nextQuestion()
        viewModel.selectAnswer("WrongChoice")
        assertEquals(1, soundPlayer.playedCorrectCount)
        assertEquals(1, soundPlayer.playedWrongCount)
    }

    @Test
    fun `useFiftyFifty hides two incorrect options`() = runTest {
        val viewModel = QuizViewModel(
            categoryId = 17,
            level = 1,
            triviaRepository = triviaRepo,
            progressRepository = progressRepo,
            reviewRepository = reviewRepo,
            settingsRepository = settingsRepo,
            quizEngine = quizEngine,
            timeProvider = { 1000L },
        )

        viewModel.useFiftyFifty()

        val state = viewModel.uiState.value
        assertEquals(1, state.hintsLeft)
        assertEquals(2, state.removedOptions.size)
        assertFalse(state.removedOptions.contains("Paris"))
    }

    @Test
    fun `flagCurrentQuestion flags question in repository and advances without review card or wrong count`() = runTest {
        val viewModel = QuizViewModel(
            categoryId = 17,
            level = 1,
            triviaRepository = triviaRepo,
            progressRepository = progressRepo,
            reviewRepository = reviewRepo,
            settingsRepository = settingsRepo,
            quizEngine = quizEngine,
            timeProvider = { 1000L },
        )

        viewModel.flagCurrentQuestion()

        assertEquals(1, triviaRepo.flaggedQuestions.size)
        assertEquals("Capital of France?", triviaRepo.flaggedQuestions[0])
        assertEquals(1, viewModel.uiState.value.totalQuestions)

        viewModel.selectAnswer("Tokyo")
        viewModel.nextQuestion()

        val result = progressRepo.getQuizResult(1L).first()
        assertNotNull(result)
        assertEquals(1, result?.totalCount)
        assertEquals(1, result?.correctCount)

        val dueCount = reviewRepo.getDueCount(1000L).first()
        assertEquals(0, dueCount)
    }

    @Test
    fun `reporting every question saves no session and sets error to ALL_REPORTED`() = runTest {
        val viewModel = QuizViewModel(
            categoryId = 17,
            level = 1,
            triviaRepository = triviaRepo,
            progressRepository = progressRepo,
            reviewRepository = reviewRepo,
            settingsRepository = settingsRepo,
            quizEngine = quizEngine,
            timeProvider = { 1000L },
        )

        viewModel.flagCurrentQuestion()
        viewModel.flagCurrentQuestion()

        val state = viewModel.uiState.value
        assertNull(state.finishedSessionId)
        assertTrue(progressRepo.quizResultsState.value.isEmpty())
        assertEquals(QuizError.ALL_REPORTED, state.error)
        assertTrue(state.showReportedMessage)
    }

    @Test
    fun `nextQuestion advances to next question and clears state`() = runTest {
        val viewModel = QuizViewModel(
            categoryId = 17,
            level = 1,
            triviaRepository = triviaRepo,
            progressRepository = progressRepo,
            reviewRepository = reviewRepo,
            settingsRepository = settingsRepo,
            quizEngine = quizEngine,
            timeProvider = { 1000L },
        )

        viewModel.selectAnswer("Paris")
        viewModel.nextQuestion()

        val state = viewModel.uiState.value
        assertEquals(2, state.questionNumber)
        assertEquals("Capital of Japan?", state.questionText)
        assertNull(state.selectedAnswer)
        assertNull(state.correctAnswer)
    }

    @Test
    fun `finishing all questions saves quiz session and sets finishedSessionId`() = runTest {
        val viewModel = QuizViewModel(
            categoryId = 17,
            level = 1,
            triviaRepository = triviaRepo,
            progressRepository = progressRepo,
            reviewRepository = reviewRepo,
            settingsRepository = settingsRepo,
            quizEngine = quizEngine,
            timeProvider = { 1000L },
        )

        viewModel.selectAnswer("Paris")
        viewModel.nextQuestion()
        viewModel.selectAnswer("WrongChoice")
        viewModel.nextQuestion()

        val state = viewModel.uiState.value
        assertNotNull(state.finishedSessionId)
        assertEquals(1L, state.finishedSessionId)

        val progress = progressRepo.getLevelProgress(17).first()
        assertEquals(1, progress.size)
        assertEquals(0, progress[0].stars)
    }

    @Test
    fun `timer triggers timeUp when time runs out`() = runTest {
        settingsRepo.updateSettings(UserSettings(timerMode = TimerMode.CHALLENGE))

        val viewModel = QuizViewModel(
            categoryId = 17,
            level = 1,
            triviaRepository = triviaRepo,
            progressRepository = progressRepo,
            reviewRepository = reviewRepo,
            settingsRepository = settingsRepo,
            quizEngine = quizEngine,
            timeProvider = { 1000L },
        )

        assertEquals(15, viewModel.uiState.value.secondsLeft)

        advanceTimeBy(16000L)

        val state = viewModel.uiState.value
        assertNotNull(state.selectedAnswer)
        assertEquals("Paris", state.correctAnswer)
    }
}
