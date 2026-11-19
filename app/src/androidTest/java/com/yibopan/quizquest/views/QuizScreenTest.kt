package com.yibopan.quizquest.views

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yibopan.quizquest.ui.theme.QuizQuestTheme
import com.yibopan.quizquest.viewmodel.QuizUIState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** UI tests for [QuizScreenContent] verifying questions, options, feedback, 50/50, leave dialog, and state descriptions. */
@RunWith(AndroidJUnit4::class)
class QuizScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysQuizScreenContentWithQuestionAndOptions() {
        val uiState = QuizUIState(
            categoryId = 17,
            level = 1,
            questionNumber = 1,
            totalQuestions = 10,
            questionText = "What is H2O?",
            options = listOf("Water", "Fire", "Earth", "Air"),
            hintsLeft = 2,
        )

        composeTestRule.setContent {
            QuizQuestTheme {
                QuizScreenContent(
                    uiState = uiState,
                    onBackPressed = {},
                    onSelectAnswer = {},
                    onFiftyFiftyClicked = {},
                    onNextClicked = {},
                    onRetryClicked = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.SCREEN_QUIZ).assertIsDisplayed()
        composeTestRule.onNodeWithText("What is H2O?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Water").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fire").assertIsDisplayed()
    }

    @Test
    fun selectingOptionShowsFeedbackText() {
        val uiState = QuizUIState(
            categoryId = 17,
            level = 1,
            questionNumber = 1,
            totalQuestions = 10,
            questionText = "What is H2O?",
            options = listOf("Water", "Fire", "Earth", "Air"),
            selectedAnswer = "Water",
            correctAnswer = "Water",
            hintsLeft = 2,
        )

        composeTestRule.setContent {
            QuizQuestTheme {
                QuizScreenContent(
                    uiState = uiState,
                    onBackPressed = {},
                    onSelectAnswer = {},
                    onFiftyFiftyClicked = {},
                    onNextClicked = {},
                    onRetryClicked = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Correct!").assertIsDisplayed()
    }

    @Test
    fun fiftyFiftyHidesTwoOptions() {
        val uiState = QuizUIState(
            categoryId = 17,
            level = 1,
            questionNumber = 1,
            totalQuestions = 10,
            questionText = "What is H2O?",
            options = listOf("Water", "Fire", "Earth", "Air"),
            removedOptions = setOf("Fire", "Earth"),
            hintsLeft = 1,
        )

        composeTestRule.setContent {
            QuizQuestTheme {
                QuizScreenContent(
                    uiState = uiState,
                    onBackPressed = {},
                    onSelectAnswer = {},
                    onFiftyFiftyClicked = {},
                    onNextClicked = {},
                    onRetryClicked = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Water").assertIsDisplayed()
        composeTestRule.onNodeWithText("Air").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fire").assertDoesNotExist()
        composeTestRule.onNodeWithText("Earth").assertDoesNotExist()
    }

    @Test
    fun leaveDialogShowsLeaveAndContinue() {
        val uiState = QuizUIState(
            categoryId = 17,
            level = 1,
            questionNumber = 1,
            totalQuestions = 10,
            questionText = "What is H2O?",
            options = listOf("Water", "Fire", "Earth", "Air"),
        )

        composeTestRule.setContent {
            QuizQuestTheme {
                QuizScreenContent(
                    uiState = uiState,
                    onBackPressed = {},
                    onSelectAnswer = {},
                    onFiftyFiftyClicked = {},
                    onNextClicked = {},
                    onRetryClicked = {},
                )
            }
        }

        // Tap top bar back button to trigger leave dialog
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        composeTestRule.onNodeWithText("Leave this level?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Leave").assertIsDisplayed()
        composeTestRule.onNodeWithText("Continue").assertIsDisplayed()
    }

    @Test
    fun displaysStateDescriptionsForCorrectAndIncorrectAnswers() {
        val uiState = QuizUIState(
            categoryId = 17,
            level = 1,
            questionNumber = 1,
            totalQuestions = 10,
            questionText = "What is the capital of France?",
            options = listOf("Paris", "London", "Berlin", "Rome"),
            selectedAnswer = "London",
            correctAnswer = "Paris",
        )

        composeTestRule.setContent {
            QuizQuestTheme {
                QuizScreenContent(
                    uiState = uiState,
                    onBackPressed = {},
                    onSelectAnswer = {},
                    onFiftyFiftyClicked = {},
                    onNextClicked = {},
                    onRetryClicked = {},
                )
            }
        }

        composeTestRule.onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Correct answer"),
        ).assertIsDisplayed()

        composeTestRule.onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Your answer, incorrect"),
        ).assertIsDisplayed()
    }
}
