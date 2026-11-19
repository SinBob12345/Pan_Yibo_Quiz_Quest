package com.yibopan.quizquest.views

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yibopan.quizquest.R
import com.yibopan.quizquest.data.model.Category
import com.yibopan.quizquest.data.model.SampleData
import com.yibopan.quizquest.ui.theme.QuizQuestTheme
import com.yibopan.quizquest.viewmodel.HomeUIState
import com.yibopan.quizquest.viewmodel.HomeViewModel
import com.yibopan.quizquest.views.components.AppTopBar
import com.yibopan.quizquest.views.components.LoadingState
import org.koin.compose.viewmodel.koinViewModel

/**
 * Landing page: today's summary, review shortcut and the list of worlds.
 * Gets its ViewModel with koinViewModel() and collects the state with
 * collectAsStateWithLifecycle() (Lectures 4 and 5).
 */
@Composable
fun HomeScreen(
    onWorldClicked: (categoryId: Int) -> Unit,
    onReviewClicked: () -> Unit,
) {
    val homeViewModel: HomeViewModel = koinViewModel()
    val homeUIState by homeViewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        homeViewModel.refresh()
        onPauseOrDispose { }
    }

    HomeScreenContent(
        uiState = homeUIState,
        onWorldClicked = onWorldClicked,
        onReviewClicked = onReviewClicked,
        onRetryClicked = { homeViewModel.retry() },
    )
}

/** Stateless Home UI (used by [HomeScreen], previews and UI tests). */
@Composable
fun HomeScreenContent(
    uiState: HomeUIState,
    onWorldClicked: (categoryId: Int) -> Unit,
    onReviewClicked: () -> Unit,
    modifier: Modifier = Modifier,
    onRetryClicked: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.testTag(TestTags.SCREEN_HOME),
        topBar = { AppTopBar(title = stringResource(R.string.app_name)) },
    ) { paddingValues ->
        // Lecture 2 & 3: Limit content width to 600 dp centered on large screens.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TodayCard(uiState = uiState)
                    if (uiState.isDailyGoalReached) {
                        DoneForTodayCard()
                    }
                    ReviewShortcutCard(dueCount = uiState.dueReviewCount, onClick = onReviewClicked)
                    Text(
                        text = stringResource(R.string.home_worlds_header),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .semantics { heading() },
                    )
                }
            }
            if (uiState.isLoading) {
                item {
                    LoadingState(modifier = Modifier.padding(vertical = 24.dp))
                }
            } else if (uiState.worldsUnavailable) {
                item {
                    WorldsUnavailableCard(
                        isRetrying = uiState.isRetrying,
                        onRetry = onRetryClicked,
                        modifier = Modifier.widthIn(max = 600.dp),
                    )
                }
            } else {
                items(uiState.worlds, key = { it.id }) { world ->
                    WorldListItem(
                        world = world,
                        onClick = { onWorldClicked(world.id) },
                        modifier = Modifier.widthIn(max = 600.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun WorldsUnavailableCard(
    isRetrying: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.home_error_worlds_unavailable),
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(
                onClick = onRetry,
                enabled = !isRetrying,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp),
            ) {
                Text(text = stringResource(R.string.action_retry))
            }
        }
    }
}

@Composable
private fun TodayCard(uiState: HomeUIState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .semantics(mergeDescendants = true) {},
        ) {
            Text(
                text = stringResource(R.string.home_today_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(
                    R.string.home_today_summary,
                    uiState.questionsAnsweredToday,
                    uiState.levelsCompletedToday,
                    uiState.dailyGoal,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(R.string.home_active_days, uiState.activeDaysThisWeek),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/** Gentle stopping cue. Information only: it never blocks or nags (A2 commitment #6). */
@Composable
private fun DoneForTodayCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = Icons.Rounded.WbSunny, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = stringResource(R.string.home_done_for_today), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ReviewShortcutCard(dueCount: Int, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .semantics(mergeDescendants = true) {}
            .clickable(onClickLabel = stringResource(R.string.click_label_review), onClick = onClick),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Rounded.Style, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.home_review_due, dueCount),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Icon(imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null)
        }
    }
}

/** One world in the list (Lecture 5/6: ElevatedCard + clickable, like PetListItem). */
@Composable
private fun WorldListItem(world: Category, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .semantics(mergeDescendants = true) {}
            .clickable(onClickLabel = stringResource(R.string.click_label_open_world), onClick = onClick),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Rounded.Public, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = world.name,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            Icon(imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Preview(name = "Phone", showBackground = true)
@Preview(name = "FontScale 200%", fontScale = 2f, showBackground = true)
@Preview(name = "Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun HomeScreenPreview() {
    QuizQuestTheme {
        HomeScreenContent(
            uiState = HomeUIState(isLoading = false, worlds = SampleData.worlds, isDailyGoalReached = true, dueReviewCount = 4),
            onWorldClicked = {},
            onReviewClicked = {},
        )
    }
}
