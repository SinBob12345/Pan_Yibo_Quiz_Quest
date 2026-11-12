package com.yibopan.quizquest

import com.yibopan.quizquest.data.model.Difficulty
import com.yibopan.quizquest.data.model.QuestionType
import com.yibopan.quizquest.data.model.RetentionPeriod
import com.yibopan.quizquest.data.model.TimerMode
import com.yibopan.quizquest.data.model.UserSettings
import com.yibopan.quizquest.data.network.NetworkResult
import com.yibopan.quizquest.data.network.asResult
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** Stage 1 checks: API value mapping, ethical defaults and the Lecture 7 asResult() helper. */
class SkeletonSanityTest {

    @Test
    fun difficultyMapsToOpenTdbValues() {
        assertEquals(listOf("easy", "medium", "hard"), Difficulty.entries.map { it.apiValue })
        assertEquals(Difficulty.HARD, Difficulty.fromApi("hard"))
        assertEquals(Difficulty.EASY, Difficulty.fromApi("unknown"))
    }

    @Test
    fun questionTypeMapsToOpenTdbValues() {
        assertEquals(QuestionType.BOOLEAN, QuestionType.fromApi("boolean"))
        assertEquals(QuestionType.MULTIPLE, QuestionType.fromApi("multiple"))
    }

    @Test
    fun defaultSettingsFollowEthicalCommitments() {
        val defaults = UserSettings()
        assertEquals("No timer pressure by default", TimerMode.OFF, defaults.timerMode)
        assertEquals("Answer history is not kept forever by default", RetentionPeriod.DAYS_90, defaults.answerRetention)
    }

    @Test
    fun asResultWrapsValuesAndErrors() = runTest {
        assertEquals(listOf(NetworkResult.Success(1)), flowOf(1).asResult().toList())

        val failing = flow<Int> { throw IllegalStateException("boom") }
        assertEquals(listOf(NetworkResult.Error("boom")), failing.asResult().toList())
    }
}
