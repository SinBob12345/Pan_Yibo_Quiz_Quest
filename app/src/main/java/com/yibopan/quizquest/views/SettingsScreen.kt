package com.yibopan.quizquest.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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
import com.yibopan.quizquest.viewmodel.SettingsUIState
import com.yibopan.quizquest.viewmodel.SettingsViewModel
import com.yibopan.quizquest.views.components.AppTopBar
import com.yibopan.quizquest.views.components.StageNotice
import org.koin.compose.viewmodel.koinViewModel

/** Learner preferences + entry to "Privacy & your data". */
@Composable
fun SettingsScreen(onPrivacyClicked: () -> Unit) {
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settingsUIState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreenContent(uiState = settingsUIState, onPrivacyClicked = onPrivacyClicked)
}

@Composable
fun SettingsScreenContent(
    uiState: SettingsUIState,
    onPrivacyClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.testTag(TestTags.SCREEN_SETTINGS),
        topBar = { AppTopBar(title = stringResource(R.string.settings_title)) },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            item {
                StageNotice(
                    stage = 6,
                    description = stringResource(R.string.dev_settings_stage),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            // Stage 6A: sound, reduce motion, timer, questions per level, daily goal, theme,
            // answer-history retention — one Material 3 ListItem per setting.
            item {
                ListItem(
                    headlineContent = { Text(text = stringResource(R.string.settings_privacy_entry)) },
                    supportingContent = { Text(text = stringResource(R.string.settings_privacy_entry_desc)) },
                    leadingContent = { Icon(imageVector = Icons.Rounded.PrivacyTip, contentDescription = null) },
                    trailingContent = {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null)
                    },
                    modifier = Modifier.clickable(
                        onClickLabel = stringResource(R.string.click_label_open),
                        onClick = onPrivacyClicked,
                    ),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    QuizQuestTheme { SettingsScreenContent(uiState = SettingsUIState(), onPrivacyClicked = {}) }
}
