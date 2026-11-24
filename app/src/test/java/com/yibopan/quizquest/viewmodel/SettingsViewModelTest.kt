package com.yibopan.quizquest.viewmodel

import com.yibopan.quizquest.data.model.RetentionPeriod
import com.yibopan.quizquest.data.model.ThemeMode
import com.yibopan.quizquest.data.model.TimerMode
import com.yibopan.quizquest.fakes.FakeSettingsRepository
import com.yibopan.quizquest.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Unit tests for [SettingsViewModel] (Lecture 4 / Lecture 7). */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var settingsRepo: FakeSettingsRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        settingsRepo = FakeSettingsRepository()
        viewModel = SettingsViewModel(
            settingsRepository = settingsRepo,
        )
    }

    @Test
    fun `initial state loads default settings`() = runTest {
        val settings = viewModel.uiState.value.settings

        assertEquals(10, settings.questionsPerLevel)
        assertEquals(TimerMode.OFF, settings.timerMode)
        assertEquals(3, settings.dailyGoalLevels)
        assertTrue(settings.soundEnabled)
        assertFalse(settings.reduceMotion)
        assertEquals(ThemeMode.SYSTEM, settings.themeMode)
        assertEquals(RetentionPeriod.DAYS_30, settings.answerRetention)
    }

    @Test
    fun `updateQuestionsPerLevel updates repository and uiState`() = runTest {
        viewModel.updateQuestionsPerLevel(15)

        assertEquals(15, viewModel.uiState.value.settings.questionsPerLevel)
        val saved = settingsRepo.getSettings().first()
        assertEquals(15, saved.questionsPerLevel)
    }

    @Test
    fun `updateTimerMode updates timer mode setting`() = runTest {
        viewModel.updateTimerMode(TimerMode.CHALLENGE)

        assertEquals(TimerMode.CHALLENGE, viewModel.uiState.value.settings.timerMode)
        val saved = settingsRepo.getSettings().first()
        assertEquals(TimerMode.CHALLENGE, saved.timerMode)
    }

    @Test
    fun `updateDailyGoal updates daily goal setting`() = runTest {
        viewModel.updateDailyGoal(5)

        assertEquals(5, viewModel.uiState.value.settings.dailyGoalLevels)
        val saved = settingsRepo.getSettings().first()
        assertEquals(5, saved.dailyGoalLevels)
    }

    @Test
    fun `updateSoundEnabled and updateReduceMotion update boolean settings`() = runTest {
        viewModel.updateSoundEnabled(false)
        viewModel.updateReduceMotion(true)

        val settings = viewModel.uiState.value.settings
        assertFalse(settings.soundEnabled)
        assertTrue(settings.reduceMotion)
    }

    @Test
    fun `updateThemeMode updates enum settings`() = runTest {
        viewModel.updateThemeMode(ThemeMode.DARK)

        val settings = viewModel.uiState.value.settings
        assertEquals(ThemeMode.DARK, settings.themeMode)
    }
}
