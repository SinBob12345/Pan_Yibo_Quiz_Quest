package com.yibopan.quizquest.fakes

import com.yibopan.quizquest.data.database.CategoryDao
import com.yibopan.quizquest.data.database.CategoryEntity
import com.yibopan.quizquest.data.database.FlaggedQuestionDao
import com.yibopan.quizquest.data.database.FlaggedQuestionEntity
import com.yibopan.quizquest.data.database.QuestionDao
import com.yibopan.quizquest.data.database.QuestionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

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

class FakeQuestionDao : QuestionDao {
    val questions = mutableListOf<QuestionEntity>()
    private var nextId = 1L

    override suspend fun getUnusedQuestions(categoryId: Int, difficulty: String, amount: Int): List<QuestionEntity> {
        return questions.filter { it.categoryId == categoryId && it.difficulty == difficulty && !it.used }
            .take(amount)
    }

    override suspend fun getQuestions(categoryId: Int, difficulty: String, amount: Int): List<QuestionEntity> {
        return questions.filter { it.categoryId == categoryId && it.difficulty == difficulty }
            .take(amount)
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
