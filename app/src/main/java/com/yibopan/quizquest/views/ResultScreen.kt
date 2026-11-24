package com.yibopan.quizquest.views

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yibopan.quizquest.R
import com.yibopan.quizquest.ui.theme.QuizQuestTheme
import com.yibopan.quizquest.viewmodel.ResultUIState
import com.yibopan.quizquest.viewmodel.ResultViewModel
import com.yibopan.quizquest.views.components.AppTopBar
import com.yibopan.quizquest.views.components.StarRow
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Result of a finished level. */
@Composable
fun ResultScreen(
    sessionId: Long,
    categoryId: Int = 0,
    level: Int = 1,
    onBackToMapClicked: () -> Unit,
    onNextLevelClicked: (categoryId: Int, level: Int) -> Unit,
) {
    val resultViewModel: ResultViewModel = koinViewModel(parameters = { parametersOf(sessionId, categoryId, level) })
    val resultUIState by resultViewModel.uiState.collectAsStateWithLifecycle()
    ResultScreenContent(
        uiState = resultUIState,
        onBackToMapClicked = onBackToMapClicked,
        onNextLevelClicked = { onNextLevelClicked(resultUIState.categoryId, resultUIState.level + 1) },
    )
}

/**
 * Stateless Result UI.
 *
 * "Back to map" and "Next level" deliberately have EQUAL visual weight (Lecture 2: Row +
 * weight) and the app never moves to the next level automatically — a natural stopping point
 * (A2 commitment #6, ACS Code of Conduct 1.2.2(d)).
 */
@Composable
fun ResultScreenContent(
    uiState: ResultUIState,
    onBackToMapClicked: () -> Unit,
    onNextLevelClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val starsState = stringResource(R.string.world_level_stars_state, uiState.stars)

    val guidanceBase = when (uiState.stars) {
        3 -> stringResource(R.string.result_guidance_3_stars)
        2 -> stringResource(R.string.result_guidance_2_stars)
        1 -> stringResource(R.string.result_guidance_1_star)
        else -> stringResource(R.string.result_guidance_0_stars)
    }

    val missedText = if (uiState.correctCount < uiState.totalCount) {
        " " + stringResource(R.string.result_guidance_missed_in_review)
    } else ""

    val guidanceText = guidanceBase + missedText

    Scaffold(
        modifier = modifier.testTag(TestTags.SCREEN_RESULT),
        topBar = { AppTopBar(title = stringResource(R.string.result_title)) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.result_score, uiState.correctCount, uiState.totalCount),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { heading() },
                )
                StarRow(
                    stars = uiState.stars,
                    starSize = 40.dp,
                    reduceMotion = uiState.reduceMotion,
                    modifier = Modifier.semantics { stateDescription = starsState },
                )
                Text(
                    text = guidanceText,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
                if (uiState.isDailyGoalReached) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    ) {
                        Text(
                            text = stringResource(R.string.home_done_for_today),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(onClick = onBackToMapClicked, modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.result_back_to_map))
                    }
                    OutlinedButton(
                        onClick = onNextLevelClicked,
                        enabled = uiState.isNextLevelUnlocked,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(R.string.result_next_level))
                    }
                }
            }
        }
    }
}

@Preview(name = "Phone", showBackground = true)
@Preview(name = "FontScale 200%", fontScale = 2f, showBackground = true)
@Preview(name = "Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun ResultScreenPreview() {
    QuizQuestTheme {
        ResultScreenContent(
            uiState = ResultUIState(
                sessionId = 1,
                categoryId = 9,
                level = 1,
                correctCount = 8,
                totalCount = 10,
                stars = 2,
                isNextLevelUnlocked = true,
                isDailyGoalReached = true,
            ),
            onBackToMapClicked = {},
            onNextLevelClicked = {},
        )
    }
}
