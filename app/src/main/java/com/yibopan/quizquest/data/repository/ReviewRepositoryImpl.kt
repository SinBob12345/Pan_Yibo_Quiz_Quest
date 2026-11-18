package com.yibopan.quizquest.data.repository

import com.yibopan.quizquest.data.database.ReviewCardDao
import com.yibopan.quizquest.data.database.ReviewCardEntity
import com.yibopan.quizquest.data.database.toReviewCard
import com.yibopan.quizquest.data.model.AnswerRecord
import com.yibopan.quizquest.data.model.ReviewCard
import com.yibopan.quizquest.data.rules.LeitnerScheduler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Manages flashcard review cards scheduled with [LeitnerScheduler] (Lecture 7).
 *
 * New cards start in Leitner box 1.
 */
class ReviewRepositoryImpl(
    private val reviewCardDao: ReviewCardDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ReviewRepository {

    override suspend fun getDueCards(nowMillis: Long): Flow<List<ReviewCard>> {
        return reviewCardDao.getDueReviewCards(nowMillis)
            .map { entities -> entities.map { it.toReviewCard() } }
    }

    override suspend fun getDueCount(nowMillis: Long): Flow<Int> {
        return reviewCardDao.getDueReviewCards(nowMillis)
            .map { it.size }
    }

    override suspend fun addWrongAnswers(answers: List<AnswerRecord>, nowMillis: Long): Unit = withContext(dispatcher) {
        val wrong = answers.filter { !it.isCorrect }
        for (answer in wrong) {
            val existing = reviewCardDao.getCardByQuestionText(answer.questionText)
            val nextReview = LeitnerScheduler.nextReviewAt(LeitnerScheduler.MIN_BOX, nowMillis)
            if (existing != null) {
                val updated = existing.copy(
                    box = LeitnerScheduler.MIN_BOX,
                    nextReviewAt = nextReview,
                    timesWrong = existing.timesWrong + 1,
                )
                reviewCardDao.update(updated)
            } else {
                val newCard = ReviewCardEntity(
                    id = 0L,
                    questionText = answer.questionText,
                    correctAnswer = answer.correctAnswer,
                    box = LeitnerScheduler.MIN_BOX,
                    nextReviewAt = nextReview,
                    timesWrong = 1,
                    createdAt = nowMillis,
                )
                reviewCardDao.insert(newCard)
            }
        }
    }

    override suspend fun updateCard(card: ReviewCard, correct: Boolean, nowMillis: Long): Unit = withContext(dispatcher) {
        val entity = reviewCardDao.getById(card.id) ?: return@withContext
        val nextBox = LeitnerScheduler.nextBox(card.box, correct)
        val nextReview = LeitnerScheduler.nextReviewAt(nextBox, nowMillis)
        val timesWrong = if (correct) card.timesWrong else card.timesWrong + 1

        val updatedEntity = entity.copy(
            box = nextBox,
            nextReviewAt = nextReview,
            timesWrong = timesWrong,
        )
        reviewCardDao.update(updatedEntity)
    }
}
