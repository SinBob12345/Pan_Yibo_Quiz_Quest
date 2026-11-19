package com.yibopan.quizquest.views

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yibopan.quizquest.ui.theme.QuizQuestTheme
import com.yibopan.quizquest.viewmodel.PrivacyUIState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** UI tests for [PrivacyScreenContent] verifying privacy notice, export, and delete dialog. */
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
        composeTestRule.onNodeWithText("What is stored").assertIsDisplayed()
        composeTestRule.onNodeWithText("Backups").assertIsDisplayed()
        composeTestRule.onNodeWithText("How long we keep it").assertIsDisplayed()
        composeTestRule.onNodeWithText("Export My Data").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete All Data").assertIsDisplayed()
    }

    @Test
    fun deleteDialogShowsKeepMyDataAndDeleteEverything() {
        var dismissClicked = false
        var confirmClicked = false
        val uiState = PrivacyUIState(showDeleteConfirmation = true)

        composeTestRule.setContent {
            QuizQuestTheme {
                PrivacyScreenContent(
                    uiState = uiState,
                    onBackPressed = {},
                    onDismissDeleteClicked = { dismissClicked = true },
                    onConfirmDeleteClicked = { confirmClicked = true },
                )
            }
        }

        composeTestRule.onNodeWithText("Delete all my data?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Keep my data").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete everything").assertIsDisplayed()

        composeTestRule.onNodeWithText("Keep my data").performClick()

        assertTrue(dismissClicked)
        assertFalse(confirmClicked)
    }
}
