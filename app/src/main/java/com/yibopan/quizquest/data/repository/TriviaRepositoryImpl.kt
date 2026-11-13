package com.yibopan.quizquest.data.repository

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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Repository for trivia categories and questions (Lectures 5 and 7).
 *
 * Implements Stage 2 networking with rate limiting, URL decoding, and token handling.
 */
class TriviaRepositoryImpl(
    private val triviaAPI: TriviaAPI,
    private val rateLimiter: RateLimiter,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : TriviaRepository {

    private val categoriesFlow = MutableStateFlow<List<Category>>(emptyList())
    private val flaggedQuestions = mutableSetOf<String>()
    private var sessionToken: String? = null

    override suspend fun getCategories(): Flow<List<Category>> = flow {
        if (categoriesFlow.value.isEmpty()) {
            fetchRemoteCategories()
        }
        emitAll(categoriesFlow)
    }

    override suspend fun fetchRemoteCategories(): Unit = withContext(dispatcher) {
        try {
            val response = rateLimiter.throttle { triviaAPI.fetchCategories() }
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val decoded = body.categories.map { it.toCategory() }
                    val allowed = CategoryFilter.filterAllowed(decoded)
                    categoriesFlow.value = allowed
                }
            }
        } catch (_: Exception) {
            // Ignore network errors
        }
    }

    override suspend fun loadQuestions(
        categoryId: Int,
        difficulty: Difficulty,
        amount: Int,
    ): NetworkResult<List<Question>> = withContext(dispatcher) {
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

            if (!response.isSuccessful) {
                return@withContext NetworkResult.Error("HTTP error: ${response.code()}")
            }

            var body = response.body()
                ?: return@withContext NetworkResult.Error("Empty response body")

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
                if (!response.isSuccessful) {
                    return@withContext NetworkResult.Error("HTTP error: ${response.code()}")
                }
                body = response.body() ?: return@withContext NetworkResult.Error("Empty response body")
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
                if (!response.isSuccessful) {
                    return@withContext NetworkResult.Error("HTTP error: ${response.code()}")
                }
                body = response.body() ?: return@withContext NetworkResult.Error("Empty response body")
            }

            when (body.responseCode) {
                OpenTdbResponseCode.SUCCESS -> {
                    val questions = body.results
                        .map { it.toQuestion(categoryId = categoryId) }
                        .filter { it.text !in flaggedQuestions }
                    NetworkResult.Success(questions)
                }
                OpenTdbResponseCode.NO_RESULTS -> NetworkResult.Error("No questions found for this level.")
                OpenTdbResponseCode.INVALID_PARAMETER -> NetworkResult.Error("Invalid request parameter.")
                OpenTdbResponseCode.RATE_LIMIT -> NetworkResult.Error("Rate limit reached. Please try again in a few seconds.")
                OpenTdbResponseCode.TOKEN_NOT_FOUND -> NetworkResult.Error("Token session expired.")
                OpenTdbResponseCode.TOKEN_EMPTY -> NetworkResult.Error("All questions for this category have been answered.")
                else -> NetworkResult.Error("API returned error code ${body.responseCode}")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to connect to trivia server")
        }
    }

    override suspend fun flagQuestion(questionText: String): Unit = withContext(dispatcher) {
        flaggedQuestions.add(questionText)
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
