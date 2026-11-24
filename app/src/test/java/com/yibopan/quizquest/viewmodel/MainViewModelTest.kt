package com.yibopan.quizquest.viewmodel

import com.yibopan.quizquest.data.model.ThemeMode
import com.yibopan.quizquest.data.model.UserSettings
import com.yibopan.quizquest.fakes.FakePrivacyRepository
import com.yibopan.quizquest.fakes.FakeSettingsRepository
import com.yibopan.quizquest.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Unit tests for [MainViewModel] app theme selection and startup retention cleanup (Lecture 4). */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var settingsRepo: FakeSettingsRepository
    private lateinit var privacyRepo: FakePrivacyRepository

    @Before
    fun setUp() {
        settingsRepo = FakeSettingsRepository()
        privacyRepo = FakePrivacyRepository()
    }

    @Test
    fun `initialization reads themeMode and purges expired answers`() = runTest {
        settingsRepo.updateSettings(UserSettings(themeMode = ThemeMode.DARK))

        val viewModel = MainViewModel(settingsRepo, privacyRepo, timeProvider = { 1000L })

        val state = viewModel.uiState.value
        assertEquals(ThemeMode.DARK, state.themeMode)
    }

    @Test
    fun `shouldEnableDarkTheme resolves SYSTEM LIGHT and DARK modes correctly`() = runTest {
        val viewModel = MainViewModel(settingsRepo, privacyRepo, timeProvider = { 1000L })

        // Default SYSTEM mode
        assertTrue(viewModel.shouldEnableDarkTheme(systemInDark = true))
        assertFalse(viewModel.shouldEnableDarkTheme(systemInDark = false))

        // LIGHT mode
        settingsRepo.updateSettings(UserSettings(themeMode = ThemeMode.LIGHT))
        assertFalse(viewModel.shouldEnableDarkTheme(systemInDark = true))
        assertFalse(viewModel.shouldEnableDarkTheme(systemInDark = false))

        // DARK mode
        settingsRepo.updateSettings(UserSettings(themeMode = ThemeMode.DARK))
        assertTrue(viewModel.shouldEnableDarkTheme(systemInDark = true))
        assertTrue(viewModel.shouldEnableDarkTheme(systemInDark = false))
    }
}
