package com.yibopan.quizquest.data.export

import com.yibopan.quizquest.data.model.AnswerRecord
import com.yibopan.quizquest.data.model.LevelProgress
import com.yibopan.quizquest.data.model.QuizResult
import com.yibopan.quizquest.data.model.ReviewCard
import com.yibopan.quizquest.data.model.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for [ExportSerializer] (Lecture 5 kotlinx.serialization). */
class ExportSerializerTest {

    @Test
    fun `toJson and fromJson serialize and deserialize UserDataExport accurately including reportedQuestions and appVersion`() {
        val export = UserDataExport(
            exportedAt = 10000L,
            appVersion = "1.0.0",
            settings = UserSettings(questionsPerLevel = 10),
            levelProgress = listOf(LevelProgress(categoryId = 17, level = 1, stars = 3, bestScore = 10)),
            sessions = listOf(QuizResult(sessionId = 1L, categoryId = 17, level = 1, correctCount = 10, totalCount = 10, stars = 3, startedAt = 1000L, durationMs = 5000L)),
            answers = listOf(AnswerRecord(questionText = "Q1", chosenAnswer = "A", correctAnswer = "A", isCorrect = true, timeTakenMs = 1200L)),
            reviewCards = listOf(ReviewCard(id = 1L, questionText = "Q2", correctAnswer = "B", box = 2, nextReviewAt = 20000L, timesWrong = 1)),
            reportedQuestions = listOf(ReportedQuestion(questionText = "Q3 Bad Question", reportedAt = 5000L)),
        )

        val jsonString = ExportSerializer.toJson(export)
        val deserialized = ExportSerializer.fromJson(jsonString)

        assertEquals(export.exportedAt, deserialized.exportedAt)
        assertEquals("1.0.0", deserialized.appVersion)
        assertEquals(export.settings.questionsPerLevel, deserialized.settings.questionsPerLevel)
        assertEquals(1, deserialized.levelProgress.size)
        assertEquals(1, deserialized.sessions.size)
        assertEquals(1, deserialized.answers.size)
        assertEquals(1, deserialized.reviewCards.size)
        assertEquals(1, deserialized.reportedQuestions.size)
        assertEquals("Q3 Bad Question", deserialized.reportedQuestions[0].questionText)
        assertEquals(5000L, deserialized.reportedQuestions[0].reportedAt)
    }

    @Test
    fun `empty reportedQuestions exports as empty list`() {
        val export = UserDataExport(
            exportedAt = 10000L,
            appVersion = "1.0.0",
            settings = UserSettings(),
            levelProgress = emptyList(),
            sessions = emptyList(),
            answers = emptyList(),
            reviewCards = emptyList(),
            reportedQuestions = emptyList(),
        )

        val jsonString = ExportSerializer.toJson(export)
        val deserialized = ExportSerializer.fromJson(jsonString)

        assertTrue(deserialized.reportedQuestions.isEmpty())
        assertTrue(jsonString.contains("\"reportedQuestions\": []"))
    }
}
