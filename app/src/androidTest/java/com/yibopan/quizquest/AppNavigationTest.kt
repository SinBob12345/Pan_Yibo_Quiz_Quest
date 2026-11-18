package com.yibopan.quizquest

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yibopan.quizquest.views.TestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Navigation integration tests (Lecture 6).
 * Verifies bottom bar tab switching, hiding tabs during play flow, and system back navigation.
 */
@RunWith(AndroidJUnit4::class)
class AppNavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun tappingTabsChangesScreen() {
        composeTestRule.onNodeWithTag(TestTags.SCREEN_HOME).assertIsDisplayed()

        composeTestRule.onNodeWithText("Review").performClick()
        composeTestRule.onNodeWithTag(TestTags.SCREEN_REVIEW).assertIsDisplayed()

        composeTestRule.onNodeWithText("Stats").performClick()
        composeTestRule.onNodeWithTag(TestTags.SCREEN_STATS).assertIsDisplayed()

        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.onNodeWithTag(TestTags.SCREEN_SETTINGS).assertIsDisplayed()

        composeTestRule.onNodeWithText("Home").performClick()
        composeTestRule.onNodeWithTag(TestTags.SCREEN_HOME).assertIsDisplayed()
    }

    @Test
    fun openingWorldHidesTabsAndBackReturnsToHome() {
        composeTestRule.onNodeWithTag(TestTags.SCREEN_HOME).assertIsDisplayed()

        // Tap the first world in the list
        composeTestRule.onNodeWithText("General Knowledge").performClick()
        composeTestRule.onNodeWithTag(TestTags.SCREEN_WORLD_MAP).assertIsDisplayed()

        // Verify bottom bar tabs are hidden during play flow
        composeTestRule.onNodeWithText("Review").assertDoesNotExist()

        // Press back
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }

        // Verify returned to Home
        composeTestRule.onNodeWithTag(TestTags.SCREEN_HOME).assertIsDisplayed()
    }
}
