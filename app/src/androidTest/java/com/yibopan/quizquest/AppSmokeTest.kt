package com.yibopan.quizquest

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yibopan.quizquest.views.TestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Stage 1 smoke test: the app starts and shows the Home screen. Run it on an emulator. */
@RunWith(AndroidJUnit4::class)
class AppSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appStartsOnHomeScreen() {
        composeRule.onNodeWithTag(TestTags.SCREEN_HOME).assertIsDisplayed()
    }
}
