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
import com.yibopan.quizquest.viewmodel.PrivacyUIState
import com.yibopan.quizquest.viewmodel.PrivacyViewModel
import com.yibopan.quizquest.views.components.AppTopBar
import com.yibopan.quizquest.views.components.StageNotice
import org.koin.compose.viewmodel.koinViewModel

/**
 * "Privacy & your data" (A2 commitments #2 and #4; Lecture 9: privacy policy, data safety,
 * feedback channel). Stage 7 fills it in.
 */
@Composable
fun PrivacyScreen(onBackPressed: () -> Unit) {
    val privacyViewModel: PrivacyViewModel = koinViewModel()
    val privacyUIState by privacyViewModel.uiState.collectAsStateWithLifecycle()
    PrivacyScreenContent(uiState = privacyUIState, onBackPressed = onBackPressed)
}

@Composable
fun PrivacyScreenContent(
    uiState: PrivacyUIState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.testTag(TestTags.SCREEN_PRIVACY),
        topBar = { AppTopBar(title = stringResource(R.string.privacy_title), onBackPressed = onBackPressed) },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
        ) {
            item { StageNotice(stage = 7, description = stringResource(R.string.dev_privacy_stage)) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PrivacyScreenPreview() {
    QuizQuestTheme { PrivacyScreenContent(uiState = PrivacyUIState(), onBackPressed = {}) }
}
