package com.yibopan.quizquest.data.database

import com.yibopan.quizquest.data.model.AnswerRecord
import com.yibopan.quizquest.data.model.Category
import com.yibopan.quizquest.data.model.Difficulty
import com.yibopan.quizquest.data.model.LevelProgress
import com.yibopan.quizquest.data.model.Question
import com.yibopan.quizquest.data.model.QuestionType
import com.yibopan.quizquest.data.model.RetentionPeriod
import com.yibopan.quizquest.data.model.ReviewCard
import com.yibopan.quizquest.data.model.ThemeMode
import com.yibopan.quizquest.data.model.TimerMode
import com.yibopan.quizquest.data.model.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for entity and domain model mapping functions. */
class EntityMappersTest {

    @Test
    fun `CategoryEntity and Category mapping`() {
        val category = Category(9, "General Knowledge")
        val entity = category.toEntity(updatedAt = 1000L)

        assertEquals(CategoryEntity(9, "General Knowledge", 1000L), entity)
        assertEquals(category, entity.toCategory())
    }

    @Test
    fun `QuestionEntity and Question mapping`() {
        val question = Question(
            id = 42L,
            categoryId = 17,
            difficulty = Difficulty.MEDIUM,
            type = QuestionType.MULTIPLE,
            text = "What is 2+2?",
            correctAnswer = "4",
            incorrectAnswers = listOf("1", "2", "3"),
        )
        val entity = question.toEntity(fetchedAt = 5000L, used = true)

        assertEquals(
            QuestionEntity(
                id = 42L,
                categoryId = 17,
                difficulty = "medium",
                type = "multiple",
                text = "What is 2+2?",
                correctAnswer = "4",
                incorrectAnswers = listOf("1", "2", "3"),
                fetchedAt = 5000L,
                used = true,
            ),
            entity,
        )
        assertEquals(question, entity.toQuestion())
    }

    @Test
    fun `LevelProgressEntity and LevelProgress mapping`() {
        val progress = LevelProgress(categoryId = 17, level = 2, stars = 3, bestScore = 10)
        val entity = progress.toEntity(updatedAt = 2000L)

        assertEquals(LevelProgressEntity(17, 2, 3, 10, 2000L), entity)
        assertEquals(progress, entity.toLevelProgress())
    }

    @Test
    fun `AnswerRecordEntity and AnswerRecord mapping`() {
        val record = AnswerRecord(
            questionText = "Q text",
            chosenAnswer = "Ans",
            correctAnswer = "Ans",
            isCorrect = true,
            timeTakenMs = 1500L,
        )
        val entity = record.toEntity(sessionId = 10L, answeredAt = 3000L)

        assertEquals(
            AnswerRecordEntity(
                id = 0L,
                sessionId = 10L,
                questionText = "Q text",
                chosenAnswer = "Ans",
                correctAnswer = "Ans",
                isCorrect = true,
                answeredAt = 3000L,
                timeTakenMs = 1500L,
            ),
            entity,
        )
        assertEquals(record, entity.toAnswerRecord())
    }

    @Test
    fun `ReviewCardEntity and ReviewCard mapping`() {
        val card = ReviewCard(
            id = 5L,
            questionText = "Q",
            correctAnswer = "A",
            box = 2,
            nextReviewAt = 8000L,
            timesWrong = 1,
        )
        val entity = card.toEntity(createdAt = 1000L)

        assertEquals(
            ReviewCardEntity(
                id = 5L,
                questionText = "Q",
                correctAnswer = "A",
                box = 2,
                nextReviewAt = 8000L,
                timesWrong = 1,
                createdAt = 1000L,
            ),
            entity,
        )
        assertEquals(card, entity.toReviewCard())
    }

    @Test
    fun `SettingsEntity and UserSettings mapping`() {
        val settings = UserSettings(
            questionsPerLevel = 15,
            timerMode = TimerMode.CHALLENGE,
            soundEnabled = false,
            reduceMotion = true,
            themeMode = ThemeMode.DARK,
            dailyGoalLevels = 5,
            answerRetention = RetentionPeriod.DAYS_30,
        )
        val entity = settings.toEntity()

        assertEquals(
            SettingsEntity(
                id = 0,
                questionsPerLevel = 15,
                timerMode = "CHALLENGE",
                soundEnabled = false,
                reduceMotion = true,
                themeMode = "DARK",
                dailyGoalLevels = 5,
                answerRetention = "DAYS_30",
            ),
            entity,
        )
        assertEquals(settings, entity.toUserSettings())
    }
}
