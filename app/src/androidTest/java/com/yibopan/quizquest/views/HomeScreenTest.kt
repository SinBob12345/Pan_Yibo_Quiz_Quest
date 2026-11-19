package com.yibopan.quizquest.views

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yibopan.quizquest.ui.theme.QuizQuestTheme
import com.yibopan.quizquest.viewmodel.HomeUIState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** UI tests for [HomeScreenContent] verifying worlds unavailable card and retry button. */
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun worldsUnavailableShowsRetryButtonAndCallsOnRetryClicked() {
        var retryClicked = false
        val uiState = HomeUIState(
            isLoading = false,
            worldsUnavailable = true,
            worlds = emptyList(),
        )

        composeTestRule.setContent {
            QuizQuestTheme {
                HomeScreenContent(
                    uiState = uiState,
                    onWorldClicked = {},
                    onReviewClicked = {},
                    onRetryClicked = { retryClicked = true },
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.SCREEN_HOME).assertIsDisplayed()
        composeTestRule.onNodeWithText("We couldn't load the worlds. Check your internet connection and try again.").assertIsDisplayed()

        composeTestRule.onNodeWithText("Try again").performClick()
        assertTrue(retryClicked)
    }
}
