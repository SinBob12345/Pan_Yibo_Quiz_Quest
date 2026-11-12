package com.yibopan.quizquest.data.repository

import com.yibopan.quizquest.data.model.AnswerRecord
import com.yibopan.quizquest.data.model.ReviewCard
import kotlinx.coroutines.flow.Flow

/** Flashcards made from wrong answers, scheduled with LeitnerScheduler. Implemented in Stage 4. */
interface ReviewRepository {

    suspend fun getDueCards(nowMillis: Long): Flow<List<ReviewCard>>

    suspend fun getDueCount(nowMillis: Long): Flow<Int>

    /** Creates a card for every incorrect answer (or increases timesWrong if it already exists). */
    suspend fun addWrongAnswers(answers: List<AnswerRecord>, nowMillis: Long)

    /** Moves the card to its next Leitner box and schedules the next review (Lecture 7: @Update). */
    suspend fun updateCard(card: ReviewCard, correct: Boolean, nowMillis: Long)
}
