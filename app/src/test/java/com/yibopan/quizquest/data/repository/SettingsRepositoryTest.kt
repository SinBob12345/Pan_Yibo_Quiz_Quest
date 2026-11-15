package com.yibopan.quizquest.data.repository

import com.yibopan.quizquest.data.model.RetentionPeriod
import com.yibopan.quizquest.data.model.TimerMode
import com.yibopan.quizquest.data.model.UserSettings
import com.yibopan.quizquest.fakes.FakeSettingsDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** Unit tests for [SettingsRepositoryImpl] (Lecture 7). */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var settingsDao: FakeSettingsDao
    private lateinit var repository: SettingsRepositoryImpl

    @Before
    fun setUp() {
        settingsDao = FakeSettingsDao()
        repository = SettingsRepositoryImpl(
            settingsDao = settingsDao,
            dispatcher = testDispatcher,
        )
    }

    @Test
    fun `getSettings emits defaults when Room is empty`() = runTest {
        val settings = repository.getSettings().first()

        assertEquals(10, settings.questionsPerLevel)
        assertEquals(TimerMode.OFF, settings.timerMode)
        assertEquals(RetentionPeriod.DAYS_90, settings.answerRetention)
    }

    @Test
    fun `updateSettings updates settings in Room`() = runTest {
        val updated = UserSettings(
            questionsPerLevel = 15,
            timerMode = TimerMode.CHALLENGE,
            soundEnabled = false,
            reduceMotion = true,
            answerRetention = RetentionPeriod.DAYS_30,
        )

        repository.updateSettings(updated)

        val loaded = repository.getSettings().first()
        assertEquals(15, loaded.questionsPerLevel)
        assertEquals(TimerMode.CHALLENGE, loaded.timerMode)
        assertEquals(RetentionPeriod.DAYS_30, loaded.answerRetention)
    }
}
