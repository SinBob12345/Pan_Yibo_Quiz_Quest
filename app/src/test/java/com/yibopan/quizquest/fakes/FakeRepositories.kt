package com.yibopan.quizquest.fakes

import com.yibopan.quizquest.data.model.AnswerRecord
import com.yibopan.quizquest.data.model.Category
import com.yibopan.quizquest.data.model.Difficulty
import com.yibopan.quizquest.data.model.LevelProgress
import com.yibopan.quizquest.data.model.Question
import com.yibopan.quizquest.data.model.QuizResult
import com.yibopan.quizquest.data.model.ReviewCard
import com.yibopan.quizquest.data.model.StatsSummary
import com.yibopan.quizquest.data.model.UserSettings
import com.yibopan.quizquest.data.network.NetworkResult
import com.yibopan.quizquest.data.repository.PrivacyRepository
import com.yibopan.quizquest.data.repository.ProgressRepository
import com.yibopan.quizquest.data.repository.ReviewRepository
import com.yibopan.quizquest.data.repository.SettingsRepository
import com.yibopan.quizquest.data.repository.StatsRepository
import com.yibopan.quizquest.data.repository.TriviaRepository
import com.yibopan.quizquest.data.rules.LeitnerScheduler
import com.yibopan.quizquest.data.rules.StarRating
import com.yibopan.quizquest.data.sound.SoundPlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory fake implementations of repository interfaces for ViewModel unit tests (Lecture 4).
 */

class FakeTriviaRepository : TriviaRepository {
    val categoriesFlow = MutableStateFlow<List<Category>>(
        listOf(
            Category(9, "General Knowledge"),
            Category(17, "Science & Nature"),
            Category(23, "History"),
        ),
    )
    var questionsResult: NetworkResult<List<Question>> = NetworkResult.Success(emptyList())
    val flaggedQuestions = mutableListOf<String>()
    var fetchRemoteCategoriesCalled = false
    var fetchRemoteCategoriesCount = 0

    override suspend fun getCategories(): Flow<List<Category>> = categoriesFlow

    override suspend fun fetchRemoteCategories() {
        fetchRemoteCategoriesCalled = true
        fetchRemoteCategoriesCount++
    }

    override suspend fun loadQuestions(
        categoryId: Int,
        difficulty: Difficulty,
        amount: Int,
    ): NetworkResult<List<Question>> = questionsResult

    override suspend fun flagQuestion(questionText: String) {
        flaggedQuestions.add(questionText)
    }
}

class FakeProgressRepository : ProgressRepository {
    val levelProgressFlow = MutableStateFlow<List<LevelProgress>>(emptyList())
    val quizResultsState = MutableStateFlow<Map<Long, QuizResult>>(emptyMap())
    private var nextSessionId = 1L
    var shouldFail = false

    override suspend fun getLevelProgress(categoryId: Int): Flow<List<LevelProgress>> {
        return levelProgressFlow.map { list -> list.filter { it.categoryId == categoryId } }
    }

    override suspend fun saveQuizResult(result: QuizResult, answers: List<AnswerRecord>): Long {
        if (shouldFail) throw IllegalStateException("Database error while saving quiz result")
        val id = if (result.sessionId == 0L) nextSessionId++ else result.sessionId
        val savedResult = result.copy(sessionId = id)
        quizResultsState.value = quizResultsState.value + (id to savedResult)

        val currentProgress = levelProgressFlow.value
        val existing = currentProgress.find { it.categoryId == result.categoryId && it.level == result.level }
        val newProgress = LevelProgress(
            categoryId = result.categoryId,
            level = result.level,
            stars = maxOf(result.stars, existing?.stars ?: 0),
            bestScore = maxOf(result.correctCount, existing?.bestScore ?: 0),
        )
        val filtered = currentProgress.filterNot { it.categoryId == result.categoryId && it.level == result.level }
        levelProgressFlow.value = filtered + newProgress

        return id
    }

    override suspend fun getQuizResult(sessionId: Long): Flow<QuizResult?> {
        return quizResultsState.map { map -> map[sessionId] }
    }

    override suspend fun getLevelsCompletedSince(startMillis: Long): Flow<Int> {
        return quizResultsState.map { map ->
            map.values.count { it.startedAt >= startMillis && it.stars >= StarRating.PASS_STARS }
        }
    }
}

class FakeReviewRepository : ReviewRepository {
    val cardsFlow = MutableStateFlow<List<ReviewCard>>(emptyList())
    private var nextCardId = 1L

    override suspend fun getDueCards(nowMillis: Long): Flow<List<ReviewCard>> {
        return cardsFlow.map { list -> list.filter { LeitnerScheduler.isDue(it.nextReviewAt, nowMillis) } }
    }

    override suspend fun getDueCount(nowMillis: Long): Flow<Int> {
        return cardsFlow.map { list -> list.count { LeitnerScheduler.isDue(it.nextReviewAt, nowMillis) } }
    }

    override suspend fun addWrongAnswers(answers: List<AnswerRecord>, nowMillis: Long) {
        val wrong = answers.filter { !it.isCorrect }
        val currentCards = cardsFlow.value.toMutableList()

        for (answer in wrong) {
            val existingIndex = currentCards.indexOfFirst { it.questionText == answer.questionText }
            if (existingIndex >= 0) {
                val existing = currentCards[existingIndex]
                currentCards[existingIndex] = existing.copy(
                    box = LeitnerScheduler.MIN_BOX,
                    nextReviewAt = LeitnerScheduler.nextReviewAt(LeitnerScheduler.MIN_BOX, nowMillis),
                    timesWrong = existing.timesWrong + 1,
                )
            } else {
                currentCards.add(
                    ReviewCard(
                        id = nextCardId++,
                        questionText = answer.questionText,
                        correctAnswer = answer.correctAnswer,
                        box = LeitnerScheduler.MIN_BOX,
                        nextReviewAt = LeitnerScheduler.nextReviewAt(LeitnerScheduler.MIN_BOX, nowMillis),
                        timesWrong = 1,
                    ),
                )
            }
        }
        cardsFlow.value = currentCards
    }

    override suspend fun updateCard(card: ReviewCard, correct: Boolean, nowMillis: Long) {
        val nextBox = LeitnerScheduler.nextBox(card.box, correct)
        val nextReview = LeitnerScheduler.nextReviewAt(nextBox, nowMillis)
        val timesWrong = if (correct) card.timesWrong else card.timesWrong + 1

        val updated = card.copy(
            box = nextBox,
            nextReviewAt = nextReview,
            timesWrong = timesWrong,
        )
        val currentCards = cardsFlow.value.filterNot { it.id == card.id }
        cardsFlow.value = currentCards + updated
    }
}

class FakeStatsRepository : StatsRepository {
    val statsSummaryFlow = MutableStateFlow(StatsSummary())

    override suspend fun getStatsSummary(): Flow<StatsSummary> = statsSummaryFlow
}

class FakeSettingsRepository : SettingsRepository {
    val settingsFlow = MutableStateFlow(UserSettings())

    override suspend fun getSettings(): Flow<UserSettings> = settingsFlow

    override suspend fun updateSettings(settings: UserSettings) {
        settingsFlow.value = settings
    }
}

class FakePrivacyRepository : PrivacyRepository {
    var exportJsonString: String = "{\"data\": \"export\"}"
    var deleteAllDataCalled = false
    var deleteExpiredAnswersResult = 0

    override suspend fun exportAllDataAsJson(): String = exportJsonString

    override suspend fun deleteAllData() {
        deleteAllDataCalled = true
    }

    override suspend fun deleteExpiredAnswers(nowMillis: Long): Int = deleteExpiredAnswersResult
}

class FakeSoundPlayer : SoundPlayer {
    var playedCorrectCount = 0
    var playedWrongCount = 0
    var released = false

    override fun playCorrect() {
        playedCorrectCount++
    }

    override fun playWrong() {
        playedWrongCount++
    }

    override fun release() {
        released = true
    }
}
