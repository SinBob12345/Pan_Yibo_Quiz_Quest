package com.yibopan.quizquest.fakes

import com.yibopan.quizquest.data.network.CategoriesResponse
import com.yibopan.quizquest.data.network.NetworkCategory
import com.yibopan.quizquest.data.network.NetworkQuestion
import com.yibopan.quizquest.data.network.OpenTdbResponseCode
import com.yibopan.quizquest.data.network.QuestionsResponse
import com.yibopan.quizquest.data.network.TokenResponse
import com.yibopan.quizquest.data.network.TriviaAPI
import retrofit2.Response
import java.io.IOException

/** Hand-written fake of [TriviaAPI] for testing [com.yibopan.quizquest.data.repository.TriviaRepositoryImpl]. */
class FakeTriviaAPI : TriviaAPI {

    var categoriesResponse: Response<CategoriesResponse> = Response.success(
        CategoriesResponse(
            categories = listOf(
                NetworkCategory(9, "General%20Knowledge"),
                NetworkCategory(10, "Entertainment%3A%20Books"),
                NetworkCategory(17, "Science%20%26%20Nature"),
                NetworkCategory(23, "History"),
            )
        )
    )

    var questionsResponseCode: Int = OpenTdbResponseCode.SUCCESS
    var responseCodesSequence: MutableList<Int> = mutableListOf()
    var questionsList: List<NetworkQuestion> = listOf(
        NetworkQuestion(
            type = "multiple",
            difficulty = "easy",
            category = "Science%20%26%20Nature",
            question = "What%20is%20H2O%3F",
            correctAnswer = "Water",
            incorrectAnswers = listOf("Fire", "Earth", "Air"),
        )
    )
    var shouldQuestionsFailNetwork: Boolean = false

    var requestedTokenCount: Int = 0
    var resetTokenCount: Int = 0
    var lastRequestedToken: String? = null

    override suspend fun fetchQuestions(
        amount: Int,
        category: Int,
        difficulty: String,
        token: String?,
        encode: String,
    ): Response<QuestionsResponse> {
        lastRequestedToken = token
        if (shouldQuestionsFailNetwork) {
            throw IOException("Network offline")
        }
        val code = if (responseCodesSequence.isNotEmpty()) {
            responseCodesSequence.removeAt(0)
        } else {
            questionsResponseCode
        }
        return Response.success(
            QuestionsResponse(
                responseCode = code,
                results = if (code == OpenTdbResponseCode.SUCCESS) questionsList else emptyList()
            )
        )
    }

    override suspend fun fetchCategories(): Response<CategoriesResponse> {
        return categoriesResponse
    }

    override suspend fun requestToken(command: String): Response<TokenResponse> {
        requestedTokenCount++
        return Response.success(TokenResponse(responseCode = 0, token = "fake_token_$requestedTokenCount"))
    }

    override suspend fun resetToken(token: String, command: String): Response<TokenResponse> {
        resetTokenCount++
        return Response.success(TokenResponse(responseCode = 0, token = "reset_token_$resetTokenCount"))
    }
}
