package com.yibopan.quizquest.views

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yibopan.quizquest.ui.theme.QuizQuestTheme
import com.yibopan.quizquest.viewmodel.PrivacyUIState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** UI tests for [PrivacyScreenContent] verifying privacy notice, export, and delete items. */
@RunWith(AndroidJUnit4::class)
class PrivacyScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysPrivacyNoticeExportAndDeleteOptions() {
        val uiState = PrivacyUIState()

        composeTestRule.setContent {
            QuizQuestTheme {
                PrivacyScreenContent(uiState = uiState, onBackPressed = {})
            }
        }

        composeTestRule.onNodeWithTag(TestTags.SCREEN_PRIVACY).assertIsDisplayed()
        composeTestRule.onNodeWithText("Data Safety Notice").assertIsDisplayed()
        composeTestRule.onNodeWithText("Export My Data").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete All Data").assertIsDisplayed()
    }
}
