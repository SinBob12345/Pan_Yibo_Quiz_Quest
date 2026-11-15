package com.yibopan.quizquest.fakes

import com.yibopan.quizquest.data.model.AnswerRecord
import com.yibopan.quizquest.data.model.Difficulty
import com.yibopan.quizquest.data.model.Question
import com.yibopan.quizquest.data.model.QuestionType
import com.yibopan.quizquest.data.model.QuizResult
import com.yibopan.quizquest.data.model.StatsSummary
import com.yibopan.quizquest.data.model.TimerMode
import com.yibopan.quizquest.data.model.UserSettings
import com.yibopan.quizquest.data.network.NetworkResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for in-memory fake repository implementations (Lecture 4). */
@OptIn(ExperimentalCoroutinesApi::class)
class FakeRepositoriesTest {

    @Test
    fun `FakeTriviaRepository returns categories and questions`() = runTest {
        val fakeRepo = FakeTriviaRepository()

        val categories = fakeRepo.getCategories().first()
        assertEquals(3, categories.size)

        fakeRepo.questionsResult = NetworkResult.Success(
            listOf(
                Question(1L, 17, Difficulty.EASY, QuestionType.MULTIPLE, "Q?", "A", listOf("B")),
            ),
        )

        val result = fakeRepo.loadQuestions(17, Difficulty.EASY, 1)
        assertTrue(result is NetworkResult.Success)

        fakeRepo.flagQuestion("Q?")
        assertEquals(listOf("Q?"), fakeRepo.flaggedQuestions)
    }

    @Test
    fun `FakeProgressRepository saves quiz result and maintains level progress`() = runTest {
        val fakeRepo = FakeProgressRepository()

        val result1 = QuizResult(0L, 17, 1, 6, 10, 1, 1000L, 10000L)
        val id1 = fakeRepo.saveQuizResult(result1, emptyList())
        assertEquals(1L, id1)

        var progress = fakeRepo.getLevelProgress(17).first()
        assertEquals(1, progress[0].stars)
        assertEquals(6, progress[0].bestScore)

        val result2 = QuizResult(0L, 17, 1, 9, 10, 2, 2000L, 10000L)
        fakeRepo.saveQuizResult(result2, emptyList())

        progress = fakeRepo.getLevelProgress(17).first()
        assertEquals(2, progress[0].stars)
        assertEquals(9, progress[0].bestScore)

        val saved = fakeRepo.getQuizResult(id1).first()
        assertNotNull(saved)
        assertEquals(6, saved?.correctCount)
    }

    @Test
    fun `FakeReviewRepository manages wrong answers and card updates`() = runTest {
        val fakeRepo = FakeReviewRepository()
        val now = 1_000_000_000L

        fakeRepo.addWrongAnswers(
            listOf(AnswerRecord("Wrong Q", "B", "A", false, 1000L)),
            nowMillis = now,
        )

        val dueCount = fakeRepo.getDueCount(now + 90_000_000L).first()
        assertEquals(1, dueCount)

        val dueCards = fakeRepo.getDueCards(now + 90_000_000L).first()
        assertEquals("Wrong Q", dueCards[0].questionText)

        val card = dueCards[0]
        fakeRepo.updateCard(card, correct = true, nowMillis = now)

        val remainingDue = fakeRepo.getDueCards(now).first()
        assertTrue(remainingDue.isEmpty())
    }

    @Test
    fun `FakeStatsRepository emits stats summary`() = runTest {
        val fakeRepo = FakeStatsRepository()
        fakeRepo.statsSummaryFlow.value = StatsSummary(totalAnswered = 50, totalCorrect = 40, totalStars = 12)

        val summary = fakeRepo.getStatsSummary().first()
        assertEquals(50, summary.totalAnswered)
        assertEquals(40, summary.totalCorrect)
        assertEquals(12, summary.totalStars)
    }

    @Test
    fun `FakeSettingsRepository manages settings settings updates`() = runTest {
        val fakeRepo = FakeSettingsRepository()

        val initial = fakeRepo.getSettings().first()
        assertEquals(10, initial.questionsPerLevel)

        val updated = UserSettings(questionsPerLevel = 15, timerMode = TimerMode.CHALLENGE)
        fakeRepo.updateSettings(updated)

        val loaded = fakeRepo.getSettings().first()
        assertEquals(15, loaded.questionsPerLevel)
        assertEquals(TimerMode.CHALLENGE, loaded.timerMode)
    }

    @Test
    fun `FakePrivacyRepository simulates data export and deletion`() = runTest {
        val fakeRepo = FakePrivacyRepository()

        val json = fakeRepo.exportAllDataAsJson()
        assertEquals("{\"data\": \"export\"}", json)

        fakeRepo.deleteAllData()
        assertTrue(fakeRepo.deleteAllDataCalled)

        fakeRepo.deleteExpiredAnswersResult = 5
        val deleted = fakeRepo.deleteExpiredAnswers(1000L)
        assertEquals(5, deleted)
    }
}
