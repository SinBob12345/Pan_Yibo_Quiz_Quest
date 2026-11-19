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
import com.yibopan.quizquest.data.sound.SoundPlayer
import com.yibopan.quizquest.util.AppLog
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
 * handles question reporting, per-question timers, sound effects, and session saving.
 */
class QuizViewModel(
    private val categoryId: Int,
    private val level: Int,
    private val triviaRepository: TriviaRepository,
    private val progressRepository: ProgressRepository,
    private val reviewRepository: ReviewRepository,
    private val settingsRepository: SettingsRepository,
    private val quizEngine: QuizEngine,
    private val soundPlayer: SoundPlayer? = null,
    private val timeProvider: TimeProvider = SystemTimeProvider,
) : ViewModel() {

    private companion object {
        private const val TAG = "QuizViewModel"
    }

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
    private var currentSecondsLeft: Int? = null
    private var userSettings: UserSettings = UserSettings()
    private var isSaving: Boolean = false

    init {
        loadQuestions()
    }

    fun retry() {
        loadQuestions()
    }

    fun retrySave() {
        _uiState.update { it.copy(saveFailed = false) }
        isSaving = false
        saveSession()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            userSettings = settingsRepository.getSettings().first()
            _uiState.update { it.copy(isLoading = true, error = null) }

            val difficulty = LevelPolicy.difficultyFor(level)
            AppLog.d(TAG, "Requesting questions for category $categoryId, level $level ($difficulty)")

            val result = triviaRepository.loadQuestions(
                categoryId = categoryId,
                difficulty = difficulty,
                amount = userSettings.questionsPerLevel,
            )

            when (result) {
                is NetworkResult.Success -> {
                    val questions = result.data
                    if (questions.isEmpty()) {
                        AppLog.d(TAG, "Empty question list returned for level $level")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = QuizError.LOAD_FAILED,
                            )
                        }
                    } else {
                        AppLog.d(TAG, "Successfully loaded ${questions.size} questions for level $level")
                        quizState = quizEngine.start(questions)
                        sessionStartTime = timeProvider.now()
                        questionStartTime = sessionStartTime
                        updateUIState()
                        startTimerIfNeeded()
                    }
                }
                is NetworkResult.Error -> {
                    AppLog.d(TAG, "Error loading questions: ${result.error}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = QuizError.LOAD_FAILED,
                        )
                    }
                }
            }
        }
    }

    fun selectAnswer(answer: String) {
        if (_uiState.value.isLoading || quizState.isAnswered || quizState.isFinished) return
        val timeTaken = timeProvider.now() - questionStartTime
        val wasCorrectCount = quizState.correctCount

        quizState = quizEngine.selectAnswer(quizState, answer, timeTaken)
        stopTimer()

        if (userSettings.soundEnabled && soundPlayer != null) {
            if (quizState.correctCount > wasCorrectCount) {
                soundPlayer.playCorrect()
            } else {
                soundPlayer.playWrong()
            }
        }

        updateUIState()
    }

    fun useFiftyFifty() {
        if (_uiState.value.isLoading || quizState.isAnswered || quizState.isFinished || quizState.hintsLeft <= 0) return
        AppLog.d(TAG, "50/50 hint used on question index ${quizState.currentIndex + 1}")
        quizState = quizEngine.useFiftyFifty(quizState)
        updateUIState()
    }

    fun flagCurrentQuestion() {
        val currentQ = quizState.currentQuestion ?: return
        stopTimer()

        viewModelScope.launch {
            try {
                triviaRepository.flagQuestion(currentQ.text)
                AppLog.d(TAG, "Learner reported question: ${currentQ.text}")
            } catch (e: Exception) {
                AppLog.e(TAG, "Failed to flag question in repository", e)
            }

            quizState = quizEngine.skipCurrent(quizState)

            if (quizState.questions.isEmpty()) {
                stopTimer()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = QuizError.ALL_REPORTED,
                        showReportedMessage = true,
                    )
                }
            } else if (quizState.isFinished) {
                _uiState.update { it.copy(showReportedMessage = true) }
                saveSession()
            } else {
                questionStartTime = timeProvider.now()
                _uiState.update { it.copy(showReportedMessage = true) }
                updateUIState()
                startTimerIfNeeded()
            }
        }
    }

    fun reportedMessageShown() {
        _uiState.update { it.copy(showReportedMessage = false) }
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
        val secondsPerQuestion = userSettings.timerMode.secondsPerQuestion ?: run {
            currentSecondsLeft = null
            return
        }
        if (quizState.isAnswered || quizState.isFinished) return

        currentSecondsLeft = secondsPerQuestion

        timerJob = viewModelScope.launch {
            while ((currentSecondsLeft ?: 0) > 0 && !quizState.isAnswered && !quizState.isFinished) {
                _uiState.update { it.copy(secondsLeft = currentSecondsLeft) }
                delay(1000L)
                currentSecondsLeft = (currentSecondsLeft ?: 0) - 1
            }
            if ((currentSecondsLeft ?: 0) <= 0 && !quizState.isAnswered && !quizState.isFinished) {
                AppLog.d(TAG, "Timer expired on question index ${quizState.currentIndex + 1}")
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
        if (isSaving && !_uiState.value.saveFailed) return
        isSaving = true
        stopTimer()
        _uiState.update { it.copy(saveFailed = false) }

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
            try {
                val sessionId = progressRepository.saveQuizResult(quizResult, quizState.answers)
                reviewRepository.addWrongAnswers(quizState.answers, nowMillis = timeProvider.now())
                AppLog.d(TAG, "Quiz session saved with ID $sessionId: ${quizState.correctCount}/${quizState.questions.size} correct ($stars stars)")
                _uiState.update { it.copy(finishedSessionId = sessionId, saveFailed = false) }
            } catch (e: Exception) {
                AppLog.e(TAG, "Failed to save quiz session result", e)
                isSaving = false
                _uiState.update { it.copy(saveFailed = true) }
            }
        }
    }

    private fun updateUIState() {
        val currentQ = quizState.currentQuestion
        val secondsPerQuestion = userSettings.timerMode.secondsPerQuestion
        val currentRecord = quizState.answers.lastOrNull { it.questionText == currentQ?.text }
        val isTimeUp = (quizState.isAnswered && currentRecord != null && currentRecord.chosenAnswer == null)

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
                secondsLeft = if (secondsPerQuestion != null && !quizState.isAnswered) currentSecondsLeft else null,
                reduceMotion = userSettings.reduceMotion,
                isTimeUp = isTimeUp,
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
