package com.yibopan.quizquest.fakes

import com.yibopan.quizquest.data.database.AnswerRecordDao
import com.yibopan.quizquest.data.database.AnswerRecordEntity
import com.yibopan.quizquest.data.database.CategoryDao
import com.yibopan.quizquest.data.database.CategoryEntity
import com.yibopan.quizquest.data.database.FlaggedQuestionDao
import com.yibopan.quizquest.data.database.FlaggedQuestionEntity
import com.yibopan.quizquest.data.database.LevelProgressDao
import com.yibopan.quizquest.data.database.LevelProgressEntity
import com.yibopan.quizquest.data.database.QuestionDao
import com.yibopan.quizquest.data.database.QuestionEntity
import com.yibopan.quizquest.data.database.QuizSessionDao
import com.yibopan.quizquest.data.database.QuizSessionEntity
import com.yibopan.quizquest.data.database.ReviewCardDao
import com.yibopan.quizquest.data.database.ReviewCardEntity
import com.yibopan.quizquest.data.database.SettingsDao
import com.yibopan.quizquest.data.database.SettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class FakeCategoryDao : CategoryDao {
    private val categoriesState = MutableStateFlow<List<CategoryEntity>>(emptyList())

    override fun getCategories(): Flow<List<CategoryEntity>> = categoriesState

    override suspend fun insertAll(categories: List<CategoryEntity>) {
        categoriesState.value = (categoriesState.value + categories).distinctBy { it.id }
    }

    override suspend fun deleteAll() {
        categoriesState.value = emptyList()
    }
}

class FakeQuestionDao(
    private val flaggedDao: FakeFlaggedQuestionDao? = null,
) : QuestionDao {
    val questions = mutableListOf<QuestionEntity>()
    private var nextId = 1L

    override suspend fun getUnusedQuestions(categoryId: Int, difficulty: String, amount: Int): List<QuestionEntity> {
        val flaggedTexts = flaggedDao?.getFlaggedQuestionTexts()?.first() ?: emptyList()
        return questions.filter {
            it.categoryId == categoryId &&
                it.difficulty == difficulty &&
                !it.used &&
                it.text !in flaggedTexts
        }.take(amount)
    }

    override suspend fun getQuestions(categoryId: Int, difficulty: String, amount: Int): List<QuestionEntity> {
        val flaggedTexts = flaggedDao?.getFlaggedQuestionTexts()?.first() ?: emptyList()
        return questions.filter {
            it.categoryId == categoryId &&
                it.difficulty == difficulty &&
                it.text !in flaggedTexts
        }.take(amount)
    }

    override suspend fun insertAll(questions: List<QuestionEntity>) {
        val withIds = questions.map {
            if (it.id == 0L) it.copy(id = nextId++) else it
        }
        this.questions.addAll(withIds)
    }

    override suspend fun markAsUsed(ids: List<Long>) {
        val updated = questions.map { if (it.id in ids) it.copy(used = true) else it }
        questions.clear()
        questions.addAll(updated)
    }

    override suspend fun deleteAll() {
        questions.clear()
        nextId = 1L
    }
}

class FakeFlaggedQuestionDao : FlaggedQuestionDao {
    private val flaggedState = MutableStateFlow<List<String>>(emptyList())

    override fun getFlaggedQuestionTexts(): Flow<List<String>> = flaggedState

    override suspend fun insert(flaggedQuestion: FlaggedQuestionEntity) {
        flaggedState.value = (flaggedState.value + flaggedQuestion.questionText).distinct()
    }

    override suspend fun delete(questionText: String) {
        flaggedState.value = flaggedState.value.filter { it != questionText }
    }
}

class FakeLevelProgressDao : LevelProgressDao {
    private val progressState = MutableStateFlow<List<LevelProgressEntity>>(emptyList())

    override fun getLevelProgressForCategory(categoryId: Int): Flow<List<LevelProgressEntity>> {
        return progressState.map { list -> list.filter { it.categoryId == categoryId }.sortedBy { it.level } }
    }

    override suspend fun getProgress(categoryId: Int, level: Int): LevelProgressEntity? {
        return progressState.value.find { it.categoryId == categoryId && it.level == level }
    }

    override fun getAllProgress(): Flow<List<LevelProgressEntity>> = progressState

    override suspend fun insert(progress: LevelProgressEntity) {
        val filtered = progressState.value.filterNot { it.categoryId == progress.categoryId && it.level == progress.level }
        progressState.value = filtered + progress
    }

    override suspend fun deleteAll() {
        progressState.value = emptyList()
    }
}

class FakeQuizSessionDao : QuizSessionDao {
    private val sessionsState = MutableStateFlow<List<QuizSessionEntity>>(emptyList())
    private var nextId = 1L

    override fun getAllSessions(): Flow<List<QuizSessionEntity>> {
        return sessionsState.map { list -> list.sortedByDescending { it.startedAt } }
    }

    override suspend fun getSessionById(sessionId: Long): QuizSessionEntity? {
        return sessionsState.value.find { it.id == sessionId }
    }

    override fun getSessionStartTimes(): Flow<List<Long>> {
        return sessionsState.map { list ->
            list.filter { it.correctCount > 0 }.sortedByDescending { it.startedAt }.map { it.startedAt }
        }
    }

    override suspend fun insert(session: QuizSessionEntity): Long {
        val id = if (session.id == 0L) nextId++ else session.id
        val withId = session.copy(id = id)
        sessionsState.value = sessionsState.value + withId
        return id
    }

    override suspend fun deleteAll() {
        sessionsState.value = emptyList()
    }
}

class FakeAnswerRecordDao : AnswerRecordDao {
    val answers = mutableListOf<AnswerRecordEntity>()
    private var nextId = 1L

    override suspend fun getAnswersForSession(sessionId: Long): List<AnswerRecordEntity> {
        return answers.filter { it.sessionId == sessionId }
    }

    override suspend fun insertAll(answers: List<AnswerRecordEntity>) {
        val withIds = answers.map {
            if (it.id == 0L) it.copy(id = nextId++) else it
        }
        this.answers.addAll(withIds)
    }

    override suspend fun deleteOlderThan(cutoffMillis: Long): Int {
        val toDelete = answers.filter { it.answeredAt < cutoffMillis }
        answers.removeAll(toDelete)
        return toDelete.size
    }

    override suspend fun deleteAll() {
        answers.clear()
    }
}

class FakeReviewCardDao(
    private val flaggedDao: FakeFlaggedQuestionDao? = null,
) : ReviewCardDao {
    private val cardsState = MutableStateFlow<List<ReviewCardEntity>>(emptyList())
    private var nextId = 1L

    override fun getAllReviewCards(): Flow<List<ReviewCardEntity>> {
        return cardsState.map { list -> list.sortedBy { it.nextReviewAt } }
    }

    override fun getDueReviewCards(nowMillis: Long): Flow<List<ReviewCardEntity>> {
        val flaggedFlow = flaggedDao?.getFlaggedQuestionTexts() ?: MutableStateFlow(emptyList())
        return combine(cardsState, flaggedFlow) { cards, flagged ->
            cards.filter { it.nextReviewAt <= nowMillis && it.questionText !in flagged }
                .sortedBy { it.nextReviewAt }
        }
    }

    override suspend fun getCardByQuestionText(questionText: String): ReviewCardEntity? {
        return cardsState.value.find { it.questionText == questionText }
    }

    override suspend fun getById(id: Long): ReviewCardEntity? {
        return cardsState.value.find { it.id == id }
    }

    override suspend fun insert(card: ReviewCardEntity): Long {
        val id = if (card.id == 0L) nextId++ else card.id
        val withId = card.copy(id = id)
        cardsState.value = cardsState.value + withId
        return id
    }

    override suspend fun update(card: ReviewCardEntity) {
        val filtered = cardsState.value.filterNot { it.id == card.id || it.questionText == card.questionText }
        cardsState.value = filtered + card
    }

    override suspend fun deleteById(id: Long) {
        cardsState.value = cardsState.value.filterNot { it.id == id }
    }

    override suspend fun deleteAll() {
        cardsState.value = emptyList()
    }
}

class FakeSettingsDao : SettingsDao {
    private val settingsState = MutableStateFlow<SettingsEntity?>(null)

    override fun getSettings(): Flow<SettingsEntity?> = settingsState

    override suspend fun getSettingsDirect(): SettingsEntity? = settingsState.value

    override suspend fun insertOrUpdate(settings: SettingsEntity) {
        settingsState.value = settings
    }

    override suspend fun deleteAll() {
        settingsState.value = null
    }
}
