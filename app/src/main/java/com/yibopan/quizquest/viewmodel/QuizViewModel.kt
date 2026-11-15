package com.yibopan.quizquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yibopan.quizquest.data.model.QuizResult
import com.yibopan.quizquest.data.model.UserSettings
import com.yibopan.quizquest.data.network.NetworkResult
import com.yibopan.quizquest.data.repository.ProgressRepository
import com.yibopan.quizquest.data.repository.ReviewRepository
import com.yibopan.quizquest.data.repository.SettingsRepository
import com.yibopan.quizquest.data.repository.TriviaRepository
import com.yibopan.quizquest.data.rules.LevelPolicy
import com.yibopan.quizquest.data.rules.QuizEngine
import com.yibopan.quizquest.data.rules.QuizState
import com.yibopan.quizquest.data.rules.StarRating
import com.yibopan.quizquest.data.rules.SystemTimeProvider
import com.yibopan.quizquest.data.rules.TimeProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Quiz screen (Lecture 4 / Lecture 5).
 *
 * Loads questions from [TriviaRepository], manages gameplay state with [QuizEngine],
 * handles optional per-question timers (off by default), and persists session results
 * and review cards upon completion.
 */
class QuizViewModel(
    private val categoryId: Int,
    private val level: Int,
    private val triviaRepository: TriviaRepository,
    private val progressRepository: ProgressRepository,
    private val reviewRepository: ReviewRepository,
    private val settingsRepository: SettingsRepository,
    private val quizEngine: QuizEngine,
    private val timeProvider: TimeProvider = SystemTimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        QuizUIState(
            categoryId = categoryId,
            level = level,
            isLoading = true,
        ),
    )
    val uiState: StateFlow<QuizUIState> get() = _uiState

    private var quizState: QuizState = QuizState()
    private var sessionStartTime: Long = 0L
    private var questionStartTime: Long = 0L
    private var timerJob: Job? = null
    private var userSettings: UserSettings = UserSettings()
    private var isSaving: Boolean = false

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            userSettings = settingsRepository.getSettings().first()
            _uiState.update { it.copy(isLoading = true, error = null) }

            val difficulty = LevelPolicy.difficultyFor(level)
            val result = triviaRepository.loadQuestions(
                categoryId = categoryId,
                difficulty = difficulty,
                amount = userSettings.questionsPerLevel,
            )

            when (result) {
                is NetworkResult.Success -> {
                    val questions = result.data
                    if (questions.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "No questions found for this level.",
                            )
                        }
                    } else {
                        quizState = quizEngine.start(questions)
                        sessionStartTime = timeProvider.now()
                        questionStartTime = sessionStartTime
                        updateUIState()
                        startTimerIfNeeded()
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.error,
                        )
                    }
                }
            }
        }
    }

    fun selectAnswer(answer: String) {
        if (_uiState.value.isLoading || quizState.isAnswered || quizState.isFinished) return
        val timeTaken = timeProvider.now() - questionStartTime
        quizState = quizEngine.selectAnswer(quizState, answer, timeTaken)
        stopTimer()
        updateUIState()
    }

    fun useFiftyFifty() {
        if (_uiState.value.isLoading || quizState.isAnswered || quizState.isFinished || quizState.hintsLeft <= 0) return
        quizState = quizEngine.useFiftyFifty(quizState)
        updateUIState()
    }

    fun nextQuestion() {
        if (!quizState.isAnswered || isSaving) return
        quizState = quizEngine.next(quizState)

        if (quizState.isFinished) {
            saveSession()
        } else {
            questionStartTime = timeProvider.now()
            updateUIState()
            startTimerIfNeeded()
        }
    }

    private fun startTimerIfNeeded() {
        stopTimer()
        val secondsPerQuestion = userSettings.timerMode.secondsPerQuestion ?: return
        if (quizState.isAnswered || quizState.isFinished) return

        timerJob = viewModelScope.launch {
            var secondsLeft = secondsPerQuestion
            while (secondsLeft > 0 && !quizState.isAnswered && !quizState.isFinished) {
                _uiState.update { it.copy(secondsLeft = secondsLeft) }
                delay(1000L)
                secondsLeft--
            }
            if (secondsLeft <= 0 && !quizState.isAnswered && !quizState.isFinished) {
                val timeTaken = timeProvider.now() - questionStartTime
                quizState = quizEngine.timeUp(quizState, timeTaken)
                updateUIState()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun saveSession() {
        if (isSaving) return
        isSaving = true
        stopTimer()

        val durationMs = timeProvider.now() - sessionStartTime
        val stars = StarRating.starsFor(quizState.correctCount, quizState.questions.size)
        val quizResult = QuizResult(
            sessionId = 0L,
            categoryId = categoryId,
            level = level,
            correctCount = quizState.correctCount,
            totalCount = quizState.questions.size,
            stars = stars,
            startedAt = sessionStartTime,
            durationMs = durationMs,
        )

        viewModelScope.launch {
            val sessionId = progressRepository.saveQuizResult(quizResult, quizState.answers)
            reviewRepository.addWrongAnswers(quizState.answers, nowMillis = timeProvider.now())
            _uiState.update { it.copy(finishedSessionId = sessionId) }
        }
    }

    private fun updateUIState() {
        val currentQ = quizState.currentQuestion
        val secondsPerQuestion = userSettings.timerMode.secondsPerQuestion

        _uiState.update {
            it.copy(
                isLoading = false,
                error = null,
                questionNumber = quizState.currentIndex + 1,
                totalQuestions = quizState.questions.size,
                questionText = currentQ?.text.orEmpty(),
                options = if (currentQ != null) quizState.options.getOrNull(quizState.currentIndex).orEmpty() else emptyList(),
                removedOptions = quizState.removedOptions,
                selectedAnswer = quizState.selectedAnswer,
                correctAnswer = if (quizState.isAnswered) currentQ?.correctAnswer else null,
                hintsLeft = quizState.hintsLeft,
                secondsLeft = if (secondsPerQuestion != null && !quizState.isAnswered) secondsPerQuestion else null,
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
