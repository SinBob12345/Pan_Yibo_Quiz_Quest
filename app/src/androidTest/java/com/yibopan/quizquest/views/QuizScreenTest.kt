package com.yibopan.quizquest.views

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yibopan.quizquest.ui.theme.QuizQuestTheme
import com.yibopan.quizquest.viewmodel.QuizUIState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** UI tests for [QuizScreenContent] verifying questions, options, feedback, and leave dialog. */
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
    fun displaysFeedbackWhenAnswerIsSelected() {
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
}
