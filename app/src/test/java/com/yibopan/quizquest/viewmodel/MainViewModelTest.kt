package com.yibopan.quizquest.viewmodel

import com.yibopan.quizquest.data.model.ThemeMode
import com.yibopan.quizquest.data.model.UserSettings
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

/** Unit tests for [MainViewModel] app theme selection (Lecture 4). */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var settingsRepo: FakeSettingsRepository

    @Before
    fun setUp() {
        settingsRepo = FakeSettingsRepository()
    }

    @Test
    fun `initialization reads themeMode from SettingsRepository`() = runTest {
        settingsRepo.updateSettings(UserSettings(themeMode = ThemeMode.DARK))

        val viewModel = MainViewModel(settingsRepo)

        val state = viewModel.uiState.value
        assertEquals(ThemeMode.DARK, state.themeMode)
    }

    @Test
    fun `shouldEnableDarkTheme resolves SYSTEM LIGHT and DARK modes correctly`() = runTest {
        val viewModel = MainViewModel(settingsRepo)

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
