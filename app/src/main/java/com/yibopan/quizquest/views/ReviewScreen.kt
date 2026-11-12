package com.yibopan.quizquest.views

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yibopan.quizquest.R
import com.yibopan.quizquest.ui.theme.QuizQuestTheme
import com.yibopan.quizquest.viewmodel.ReviewUIState
import com.yibopan.quizquest.viewmodel.ReviewViewModel
import com.yibopan.quizquest.views.components.AppTopBar
import com.yibopan.quizquest.views.components.StageNotice
import org.koin.compose.viewmodel.koinViewModel

/** Flashcard review of wrongly answered questions. */
@Composable
fun ReviewScreen() {
    val reviewViewModel: ReviewViewModel = koinViewModel()
    val reviewUIState by reviewViewModel.uiState.collectAsStateWithLifecycle()
    ReviewScreenContent(uiState = reviewUIState)
}

@Composable
fun ReviewScreenContent(uiState: ReviewUIState, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.testTag(TestTags.SCREEN_REVIEW),
        topBar = { AppTopBar(title = stringResource(R.string.review_title)) },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
        ) {
            item { StageNotice(stage = 6, description = stringResource(R.string.dev_review_stage)) }
            // Stage 6C: one card at a time for uiState.currentCard, "Show answer" with
            // AnimatedVisibility (Lecture 5), two equal-weight buttons.
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewScreenPreview() {
    QuizQuestTheme { ReviewScreenContent(uiState = ReviewUIState()) }
}
