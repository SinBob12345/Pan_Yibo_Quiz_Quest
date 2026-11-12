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
import com.yibopan.quizquest.viewmodel.StatsUIState
import com.yibopan.quizquest.viewmodel.StatsViewModel
import com.yibopan.quizquest.views.components.AppTopBar
import com.yibopan.quizquest.views.components.StageNotice
import org.koin.compose.viewmodel.koinViewModel

/** User statistics: totals, accuracy per world, last 7 days, recent results. */
@Composable
fun StatsScreen() {
    val statsViewModel: StatsViewModel = koinViewModel()
    val statsUIState by statsViewModel.uiState.collectAsStateWithLifecycle()
    StatsScreenContent(uiState = statsUIState)
}

@Composable
fun StatsScreenContent(uiState: StatsUIState, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.testTag(TestTags.SCREEN_STATS),
        topBar = { AppTopBar(title = stringResource(R.string.stats_title)) },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
        ) {
            item { StageNotice(stage = 6, description = stringResource(R.string.dev_stats_stage)) }
            // Stage 6B: summary cards, accuracy per world with LinearProgressIndicator rows,
            // last 7 days, recent results. No streak counter.
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatsScreenPreview() {
    QuizQuestTheme { StatsScreenContent(uiState = StatsUIState()) }
}
