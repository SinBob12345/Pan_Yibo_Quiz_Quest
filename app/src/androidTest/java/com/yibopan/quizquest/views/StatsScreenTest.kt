package com.yibopan.quizquest.views

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yibopan.quizquest.data.model.CategoryAccuracy
import com.yibopan.quizquest.data.model.QuizResult
import com.yibopan.quizquest.data.model.StatsSummary
import com.yibopan.quizquest.ui.theme.QuizQuestTheme
import com.yibopan.quizquest.viewmodel.StatsUIState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** UI tests for [StatsScreenContent] verifying overview totals, world accuracy, and daily activity. */
@RunWith(AndroidJUnit4::class)
class StatsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysStatsOverviewAndWorldAccuracy() {
        val summary = StatsSummary(
            totalAnswered = 50,
            totalCorrect = 40,
            totalStars = 12,
            activeDaysThisWeek = 3,
            byCategory = listOf(
                CategoryAccuracy(categoryId = 17, categoryName = "Science & Nature", answered = 20, correct = 16),
            ),
        )
        val uiState = StatsUIState(isLoading = false, summary = summary)

        composeTestRule.setContent {
            QuizQuestTheme {
                StatsScreenContent(uiState = uiState)
            }
        }

        composeTestRule.onNodeWithTag(TestTags.SCREEN_STATS).assertIsDisplayed()
        composeTestRule.onNodeWithText("Overview").assertIsDisplayed()
        composeTestRule.onNodeWithText("Questions answered: 50").assertIsDisplayed()
        composeTestRule.onNodeWithText("Overall accuracy: 80%").assertIsDisplayed()
        composeTestRule.onNodeWithText("Science & Nature").assertIsDisplayed()
    }

    @Test
    fun displaysRecentAttemptsWithRealCategoryNameOrFallback() {
        val summary = StatsSummary(
            totalAnswered = 10,
            byCategory = listOf(
                CategoryAccuracy(categoryId = 17, categoryName = "Science & Nature", answered = 10, correct = 8),
            ),
            recentResults = listOf(
                QuizResult(1L, 17, 1, 8, 10, 2, 1000L, 5000L),
                QuizResult(2L, 99, 1, 5, 10, 1, 2000L, 5000L),
            ),
        )
        val uiState = StatsUIState(isLoading = false, summary = summary)

        composeTestRule.setContent {
            QuizQuestTheme {
                StatsScreenContent(uiState = uiState)
            }
        }

        composeTestRule.onNodeWithText("Science & Nature Level 1 · 8/10 correct").assertIsDisplayed()
        composeTestRule.onNodeWithText("World 99 Level 1 · 5/10 correct").assertIsDisplayed()
    }
}
