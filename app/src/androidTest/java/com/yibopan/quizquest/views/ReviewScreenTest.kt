package com.yibopan.quizquest.views

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yibopan.quizquest.data.model.ReviewCard
import com.yibopan.quizquest.ui.theme.QuizQuestTheme
import com.yibopan.quizquest.viewmodel.ReviewUIState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** UI tests for [ReviewScreenContent] verifying flashcard presentation and action controls. */
@RunWith(AndroidJUnit4::class)
class ReviewScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysFlashcardWithShowAnswerButton() {
        val card = ReviewCard(id = 1L, questionText = "What is H2O?", correctAnswer = "Water", box = 1, nextReviewAt = 1000L, timesWrong = 1)
        val uiState = ReviewUIState(isLoading = false, dueCards = listOf(card), isAnswerRevealed = false)

        composeTestRule.setContent {
            QuizQuestTheme {
                ReviewScreenContent(uiState = uiState)
            }
        }

        composeTestRule.onNodeWithTag(TestTags.SCREEN_REVIEW).assertIsDisplayed()
        composeTestRule.onNodeWithText("What is H2O?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Show answer").assertIsDisplayed()
    }

    @Test
    fun displaysRevealedAnswerAndEqualWeightButtons() {
        val card = ReviewCard(id = 1L, questionText = "What is H2O?", correctAnswer = "Water", box = 1, nextReviewAt = 1000L, timesWrong = 1)
        val uiState = ReviewUIState(isLoading = false, dueCards = listOf(card), isAnswerRevealed = true)

        composeTestRule.setContent {
            QuizQuestTheme {
                ReviewScreenContent(uiState = uiState)
            }
        }

        composeTestRule.onNodeWithText("Water").assertIsDisplayed()
        composeTestRule.onNodeWithText("Not yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("I knew it").assertIsDisplayed()
    }

    @Test
    fun displaysAllCaughtUpCardWhenNoCardsDue() {
        val uiState = ReviewUIState(isLoading = false, dueCards = emptyList())

        composeTestRule.setContent {
            QuizQuestTheme {
                ReviewScreenContent(uiState = uiState)
            }
        }

        composeTestRule.onNodeWithText("All caught up!").assertIsDisplayed()
    }
}
