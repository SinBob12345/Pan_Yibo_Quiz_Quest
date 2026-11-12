package com.yibopan.quizquest.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.yibopan.quizquest.viewmodel.QuizUIState
import com.yibopan.quizquest.viewmodel.QuizViewModel
import com.yibopan.quizquest.views.components.AppTopBar
import com.yibopan.quizquest.views.components.StageNotice
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The main learning activity ("Activity Screen" in the brief).
 * Stage 5B adds the question, options, feedback, 50/50 hint, optional timer, a leave dialog,
 * and navigation to the Result screen when uiState.finishedSessionId is set.
 */
@Composable
fun QuizScreen(
    categoryId: Int,
    level: Int,
    onBackPressed: () -> Unit,
    onQuizFinished: (sessionId: Long) -> Unit,
) {
    val quizViewModel: QuizViewModel = koinViewModel(parameters = { parametersOf(categoryId, level) })
    val quizUIState by quizViewModel.uiState.collectAsStateWithLifecycle()
    QuizScreenContent(
        uiState = quizUIState,
        onBackPressed = onBackPressed,
        onFinishDemoClicked = { onQuizFinished(DEMO_SESSION_ID) },
    )
}

/** Stage 1 placeholder session id; removed in Stage 5B. */
private const val DEMO_SESSION_ID = 0L

@Composable
fun QuizScreenContent(
    uiState: QuizUIState,
    onBackPressed: () -> Unit,
    onFinishDemoClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.testTag(TestTags.SCREEN_QUIZ),
        topBar = {
            AppTopBar(title = stringResource(R.string.quiz_title, uiState.level), onBackPressed = onBackPressed)
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StageNotice(stage = 5, description = stringResource(R.string.dev_quiz_stage))
            Button(onClick = onFinishDemoClicked, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.quiz_finish_demo))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuizScreenPreview() {
    QuizQuestTheme {
        QuizScreenContent(uiState = QuizUIState(categoryId = 9, level = 1), onBackPressed = {}, onFinishDemoClicked = {})
    }
}
