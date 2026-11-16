package com.yibopan.quizquest.views

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yibopan.quizquest.data.model.UserSettings
import com.yibopan.quizquest.ui.theme.QuizQuestTheme
import com.yibopan.quizquest.viewmodel.SettingsUIState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** UI tests for [SettingsScreenContent] verifying setting rows and options. */
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysSettingsRowsAndOptions() {
        val uiState = SettingsUIState(settings = UserSettings(questionsPerLevel = 10))

        composeTestRule.setContent {
            QuizQuestTheme {
                SettingsScreenContent(
                    uiState = uiState,
                    onPrivacyClicked = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.SCREEN_SETTINGS).assertIsDisplayed()
        composeTestRule.onNodeWithText("Sound effects").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reduce motion").assertIsDisplayed()
        composeTestRule.onNodeWithText("Questions per level").assertIsDisplayed()
        composeTestRule.onNodeWithText("Privacy & your data").assertIsDisplayed()
    }
}
