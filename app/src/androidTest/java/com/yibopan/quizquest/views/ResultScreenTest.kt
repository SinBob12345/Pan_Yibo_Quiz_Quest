package com.yibopan.quizquest.views

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yibopan.quizquest.ui.theme.QuizQuestTheme
import com.yibopan.quizquest.viewmodel.ResultUIState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** UI tests for [ResultScreenContent] verifying score, equal-weight buttons, and next level lock states. */
@RunWith(AndroidJUnit4::class)
class ResultScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysScoreAndBothActionButtons() {
        val uiState = ResultUIState(
            sessionId = 1L,
            categoryId = 17,
            level = 1,
            correctCount = 8,
            totalCount = 10,
            stars = 2,
            isNextLevelUnlocked = true,
            isDailyGoalReached = false,
        )

        composeTestRule.setContent {
            QuizQuestTheme {
                ResultScreenContent(
                    uiState = uiState,
                    onBackToMapClicked = {},
                    onNextLevelClicked = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.SCREEN_RESULT).assertIsDisplayed()
        composeTestRule.onNodeWithText("8 of 10 correct").assertIsDisplayed()
        composeTestRule.onNodeWithText("Back to map").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next level").assertIsDisplayed()
    }

    @Test
    fun nextLevelDisabledWhenLocked() {
        val uiState = ResultUIState(
            sessionId = 1L,
            categoryId = 17,
            level = 1,
            correctCount = 4,
            totalCount = 10,
            stars = 0,
            isNextLevelUnlocked = false,
            isDailyGoalReached = false,
        )

        composeTestRule.setContent {
            QuizQuestTheme {
                ResultScreenContent(
                    uiState = uiState,
                    onBackToMapClicked = {},
                    onNextLevelClicked = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Next level").assertIsNotEnabled()
    }

    @Test
    fun displaysDoneForTodayCardWhenDailyGoalIsReached() {
        val uiState = ResultUIState(
            sessionId = 1L,
            categoryId = 17,
            level = 1,
            correctCount = 8,
            totalCount = 10,
            stars = 2,
            isNextLevelUnlocked = true,
            isDailyGoalReached = true,
        )

        composeTestRule.setContent {
            QuizQuestTheme {
                ResultScreenContent(
                    uiState = uiState,
                    onBackToMapClicked = {},
                    onNextLevelClicked = {},
                )
            }
        }

        composeTestRule.onNodeWithText("You reached today's goal. Great work. It is a good time to take a break!").assertIsDisplayed()
    }
}
