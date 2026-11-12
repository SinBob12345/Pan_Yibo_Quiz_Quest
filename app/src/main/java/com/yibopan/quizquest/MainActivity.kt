package com.yibopan.quizquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yibopan.quizquest.navigation.AppNavigation
import com.yibopan.quizquest.ui.theme.QuizQuestTheme

/** Single activity. All screens are composables inside [AppNavigation] (Lecture 6). */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Stage 6A: read the theme setting through a small MainViewModel and pass darkTheme here.
            QuizQuestTheme {
                AppNavigation()
            }
        }
    }
}
