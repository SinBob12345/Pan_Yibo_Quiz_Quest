package com.yibopan.quizquest.views

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yibopan.quizquest.data.model.Difficulty
import com.yibopan.quizquest.ui.theme.QuizQuestTheme
import com.yibopan.quizquest.viewmodel.LevelItem
import com.yibopan.quizquest.viewmodel.WorldMapUIState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** UI tests for [WorldMapScreenContent] verifying grid rendering, stars, and level labels. */
@RunWith(AndroidJUnit4::class)
class WorldMapScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysWorldMapWithLevels() {
        val uiState = WorldMapUIState(
            categoryId = 17,
            worldName = "Science & Nature",
            levels = listOf(
                LevelItem(level = 1, difficulty = Difficulty.EASY, stars = 3, unlocked = true),
                LevelItem(level = 2, difficulty = Difficulty.EASY, stars = 0, unlocked = false),
            ),
        )

        composeTestRule.setContent {
            QuizQuestTheme {
                WorldMapScreenContent(
                    uiState = uiState,
                    onBackPressed = {},
                    onLevelClicked = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTags.SCREEN_WORLD_MAP).assertIsDisplayed()
        composeTestRule.onNodeWithText("Science & Nature").assertIsDisplayed()
        composeTestRule.onNodeWithText("Level 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Level 2").assertIsDisplayed()
    }
}
