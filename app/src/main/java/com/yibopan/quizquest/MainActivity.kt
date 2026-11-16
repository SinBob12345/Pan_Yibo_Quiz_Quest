package com.yibopan.quizquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yibopan.quizquest.data.model.ThemeMode
import com.yibopan.quizquest.navigation.AppNavigation
import com.yibopan.quizquest.ui.theme.QuizQuestTheme
import com.yibopan.quizquest.viewmodel.MainViewModel
import org.koin.androidx.compose.koinViewModel

/** Single activity. All screens are composables inside [AppNavigation] (Lecture 6). */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = koinViewModel()
            val mainUIState by mainViewModel.uiState.collectAsStateWithLifecycle()

            val systemInDark = isSystemInDarkTheme()
            val darkTheme = when (mainUIState.themeMode) {
                ThemeMode.SYSTEM -> systemInDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Dynamic color stays off to preserve contrast guarantees (A2 Commitment #7)
            QuizQuestTheme(darkTheme = darkTheme, dynamicColor = false) {
                AppNavigation()
            }
        }
    }
}
