package com.yibopan.quizquest.data.repository

import com.yibopan.quizquest.data.database.CategoryDao
import com.yibopan.quizquest.data.database.FlaggedQuestionDao
import com.yibopan.quizquest.data.database.FlaggedQuestionEntity
import com.yibopan.quizquest.data.database.QuestionDao
import com.yibopan.quizquest.data.database.toCategory
import com.yibopan.quizquest.data.database.toEntity
import com.yibopan.quizquest.data.database.toQuestion
import com.yibopan.quizquest.data.model.Category
import com.yibopan.quizquest.data.model.Difficulty
import com.yibopan.quizquest.data.model.Question
import com.yibopan.quizquest.data.network.NetworkResult
import com.yibopan.quizquest.data.network.OpenTdbResponseCode
import com.yibopan.quizquest.data.network.RateLimiter
import com.yibopan.quizquest.data.network.TriviaAPI
import com.yibopan.quizquest.data.network.toCategory
import com.yibopan.quizquest.data.network.toQuestion
import com.yibopan.quizquest.data.rules.CategoryFilter
import com.yibopan.quizquest.data.rules.SystemTimeProvider
import com.yibopan.quizquest.data.rules.TimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository for trivia categories and questions (Lectures 5 and 7).
 *
 * Implements Stage 2 (network) + Stage 4 (Room, offline-first).
 */
class TriviaRepositoryImpl(
    private val triviaAPI: TriviaAPI,
    private val rateLimiter: RateLimiter,
    private val categoryDao: CategoryDao,
    private val questionDao: QuestionDao,
    private val flaggedQuestionDao: FlaggedQuestionDao,
    private val timeProvider: TimeProvider = SystemTimeProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : TriviaRepository {

    private var sessionToken: String? = null

    override suspend fun getCategories(): Flow<List<Category>> = flow {
        val current = categoryDao.getCategories().first()
        if (current.isEmpty()) {
            fetchRemoteCategories()
        }
        emitAll(categoryDao.getCategories().map { list -> list.map { it.toCategory() } })
    }

    override suspend fun fetchRemoteCategories(): Unit = withContext(dispatcher) {
        try {
            val response = rateLimiter.throttle { triviaAPI.fetchCategories() }
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val now = timeProvider.now()
                    val decoded = body.categories.map { it.toCategory() }
                    val allowed = CategoryFilter.filterAllowed(decoded)
                    val entities = allowed.map { it.toEntity(updatedAt = now) }
                    categoryDao.insertAll(entities)
                }
            }
        } catch (_: Exception) {
            // Offline - categories remain in Room
        }
    }

    override suspend fun loadQuestions(
        categoryId: Int,
        difficulty: Difficulty,
        amount: Int,
    ): NetworkResult<List<Question>> = withContext(dispatcher) {
        val now = timeProvider.now()
        try {
            var token = getOrFetchToken()
            var response = rateLimiter.throttle {
                triviaAPI.fetchQuestions(
                    amount = amount,
                    category = categoryId,
                    difficulty = difficulty.apiValue,
                    token = token,
                )
            }

            if (response.isSuccessful && response.body() != null) {
                var body = response.body()!!

                // Handle token expiration (3) or depletion (4) with one retry
                if (body.responseCode == OpenTdbResponseCode.TOKEN_NOT_FOUND) {
                    sessionToken = null
                    token = getOrFetchToken()
                    response = rateLimiter.throttle {
                        triviaAPI.fetchQuestions(
                            amount = amount,
                            category = categoryId,
                            difficulty = difficulty.apiValue,
                            token = token,
                        )
                    }
                    if (response.isSuccessful && response.body() != null) {
                        body = response.body()!!
                    }
                } else if (body.responseCode == OpenTdbResponseCode.TOKEN_EMPTY && token != null) {
                    token = resetOrNewToken(token)
                    response = rateLimiter.throttle {
                        triviaAPI.fetchQuestions(
                            amount = amount,
                            category = categoryId,
                            difficulty = difficulty.apiValue,
                            token = token,
                        )
                    }
                    if (response.isSuccessful && response.body() != null) {
                        body = response.body()!!
                    }
                }

                if (body.responseCode == OpenTdbResponseCode.SUCCESS && body.results.isNotEmpty()) {
                    val questions = body.results.map { it.toQuestion(categoryId = categoryId) }

                    // Save downloaded questions to Room
                    val entities = questions.map { it.toEntity(fetchedAt = now, used = false) }
                    questionDao.insertAll(entities)

                    // Retrieve unused questions from Room (filters out flagged questions via SQL)
                    val storedUnused = questionDao.getUnusedQuestions(categoryId, difficulty.apiValue, amount)
                    if (storedUnused.isNotEmpty()) {
                        val idsToMark = storedUnused.map { it.id }.filter { it != 0L }
                        if (idsToMark.isNotEmpty()) {
                            questionDao.markAsUsed(idsToMark)
                        }
                        val resultQuestions = storedUnused.map { it.toQuestion() }
                        return@withContext NetworkResult.Success(resultQuestions)
                    }

                    return@withContext NetworkResult.Success(questions)
                }
            }

            loadOfflineQuestions(categoryId, difficulty, amount)
        } catch (_: Exception) {
            loadOfflineQuestions(categoryId, difficulty, amount)
        }
    }

    private suspend fun loadOfflineQuestions(
        categoryId: Int,
        difficulty: Difficulty,
        amount: Int,
    ): NetworkResult<List<Question>> {
        val unused = questionDao.getUnusedQuestions(categoryId, difficulty.apiValue, amount)
        val entities = if (unused.isNotEmpty()) {
            unused
        } else {
            questionDao.getQuestions(categoryId, difficulty.apiValue, amount)
        }

        if (entities.isNotEmpty()) {
            val idsToMark = entities.map { it.id }.filter { it != 0L }
            if (idsToMark.isNotEmpty()) {
                questionDao.markAsUsed(idsToMark)
            }
            val questions = entities.map { it.toQuestion() }
            return NetworkResult.Success(questions)
        }

        return NetworkResult.Error("No questions available offline. Please check your internet connection.")
    }

    override suspend fun flagQuestion(questionText: String): Unit = withContext(dispatcher) {
        val entity = FlaggedQuestionEntity(
            questionText = questionText,
            flaggedAt = timeProvider.now(),
        )
        flaggedQuestionDao.insert(entity)
    }

    private suspend fun getOrFetchToken(): String? {
        val current = sessionToken
        if (current != null) return current

        return try {
            val response = rateLimiter.throttle { triviaAPI.requestToken() }
            if (response.isSuccessful && response.body()?.responseCode == OpenTdbResponseCode.SUCCESS) {
                val newToken = response.body()?.token
                sessionToken = newToken
                newToken
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun resetOrNewToken(currentToken: String): String? {
        return try {
            val resetResponse = rateLimiter.throttle { triviaAPI.resetToken(currentToken) }
            if (resetResponse.isSuccessful && resetResponse.body()?.responseCode == OpenTdbResponseCode.SUCCESS) {
                val newToken = resetResponse.body()?.token ?: currentToken
                sessionToken = newToken
                newToken
            } else {
                sessionToken = null
                getOrFetchToken()
            }
        } catch (_: Exception) {
            sessionToken = null
            getOrFetchToken()
        }
    }
}
