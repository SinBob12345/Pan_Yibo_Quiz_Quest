package com.yibopan.quizquest.data.network

import com.yibopan.quizquest.data.model.Category
import com.yibopan.quizquest.data.model.Difficulty
import com.yibopan.quizquest.data.model.Question
import com.yibopan.quizquest.data.model.QuestionType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// JSON models for Open Trivia DB (Lecture 5: @Serializable + @SerialName).

/** Values of `response_code` returned by Open Trivia DB. */
object OpenTdbResponseCode {
    const val SUCCESS = 0
    const val NO_RESULTS = 1
    const val INVALID_PARAMETER = 2
    const val TOKEN_NOT_FOUND = 3
    const val TOKEN_EMPTY = 4
    const val RATE_LIMIT = 5
}

@Serializable
data class QuestionsResponse(
    @SerialName("response_code") val responseCode: Int,
    @SerialName("results") val results: List<NetworkQuestion> = emptyList(),
)

/** One question exactly as the API returns it. With encode=url3986 every string is URL-encoded. */
@Serializable
data class NetworkQuestion(
    @SerialName("type") val type: String,
    @SerialName("difficulty") val difficulty: String,
    @SerialName("category") val category: String,
    @SerialName("question") val question: String,
    @SerialName("correct_answer") val correctAnswer: String,
    @SerialName("incorrect_answers") val incorrectAnswers: List<String>,
)

@Serializable
data class CategoriesResponse(
    @SerialName("trivia_categories") val categories: List<NetworkCategory> = emptyList(),
)

@Serializable
data class NetworkCategory(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
)

@Serializable
data class TokenResponse(
    @SerialName("response_code") val responseCode: Int,
    @SerialName("response_message") val responseMessage: String? = null,
    @SerialName("token") val token: String? = null,
)

/** Maps a [NetworkCategory] to a domain [Category], decoding the URL3986 name. */
fun NetworkCategory.toCategory(): Category = Category(
    id = id,
    name = TextDecoder.decodeUrl3986(name),
)

/** Maps a [NetworkQuestion] to a domain [Question], decoding all URL3986 text fields. */
fun NetworkQuestion.toQuestion(categoryId: Int = 0, id: Long = 0L): Question = Question(
    id = id,
    categoryId = categoryId,
    difficulty = Difficulty.fromApi(TextDecoder.decodeUrl3986(difficulty)),
    type = QuestionType.fromApi(TextDecoder.decodeUrl3986(type)),
    text = TextDecoder.decodeUrl3986(question),
    correctAnswer = TextDecoder.decodeUrl3986(correctAnswer),
    incorrectAnswers = incorrectAnswers.map { TextDecoder.decodeUrl3986(it) },
)
