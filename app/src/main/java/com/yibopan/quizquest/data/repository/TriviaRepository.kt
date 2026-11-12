package com.yibopan.quizquest.data.repository

import com.yibopan.quizquest.data.model.Category
import com.yibopan.quizquest.data.model.Difficulty
import com.yibopan.quizquest.data.model.Question
import com.yibopan.quizquest.data.network.NetworkResult
import kotlinx.coroutines.flow.Flow

/**
 * Categories and questions (Lecture 4 repository pattern; Lectures 5 and 7 data sources).
 * Implemented in TriviaRepositoryImpl: Stage 2 (network) and Stage 4 (Room, offline first).
 */
interface TriviaRepository {

    /** Allowed categories from Room. If Room is empty, fetches them first (Lecture 7 offline-first). */
    suspend fun getCategories(): Flow<List<Category>>

    /** Downloads the category list, filters it with CategoryFilter and saves it. */
    suspend fun fetchRemoteCategories()

    /**
     * [amount] questions for one level. Network first; if the network fails, unused questions
     * saved in Room are used so the app keeps working offline.
     */
    suspend fun loadQuestions(categoryId: Int, difficulty: Difficulty, amount: Int): NetworkResult<List<Question>>

    /** Hides a question the learner reports as wrong or unsuitable. Stored on the device only. */
    suspend fun flagQuestion(questionText: String)
}
