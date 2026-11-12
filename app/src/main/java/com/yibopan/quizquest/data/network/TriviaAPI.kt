package com.yibopan.quizquest.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API interface for Open Trivia DB (Lecture 5: "Creating the API class").
 * Each function is `suspend` and returns `Response<T>`, so the repository can check
 * `response.isSuccessful` before reading `response.body()`.
 *
 * Facts from https://opentdb.com/api_config.php that shape the repository:
 *  - no API key; data licensed CC BY-SA 4.0 (credit it in the app and README)
 *  - max 50 questions per call, one category per call
 *  - each IP may call the API only once every 5 seconds -> always go through RateLimiter
 *  - session tokens stop repeated questions and expire after 6 hours of inactivity
 *  - response codes: see [OpenTdbResponseCode]
 *
 * Only non-personal data is sent. The server can still see the device's IP address —
 * the Privacy screen must say so (A2 commitment #4).
 */
interface TriviaAPI {

    @GET("api.php")
    suspend fun fetchQuestions(
        @Query("amount") amount: Int,
        @Query("category") category: Int,
        @Query("difficulty") difficulty: String,
        @Query("token") token: String? = null,
        @Query("encode") encode: String = ENCODING_URL3986,
    ): Response<QuestionsResponse>

    @GET("api_category.php")
    suspend fun fetchCategories(): Response<CategoriesResponse>

    @GET("api_token.php")
    suspend fun requestToken(@Query("command") command: String = "request"): Response<TokenResponse>

    @GET("api_token.php")
    suspend fun resetToken(
        @Query("token") token: String,
        @Query("command") command: String = "reset",
    ): Response<TokenResponse>

    companion object {
        const val BASE_URL = "https://opentdb.com/"

        /** RFC 3986 URL encoding: decode every text field with TextDecoder.decodeUrl3986. */
        const val ENCODING_URL3986 = "url3986"

        const val MAX_QUESTIONS_PER_CALL = 50
    }
}
