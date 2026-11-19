package com.yibopan.quizquest.views

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.yibopan.quizquest.R
import com.yibopan.quizquest.data.model.CategoryAccuracy
import com.yibopan.quizquest.data.model.DailyActivity
import com.yibopan.quizquest.data.model.QuizResult
import com.yibopan.quizquest.data.model.StatsSummary
import com.yibopan.quizquest.ui.theme.QuizQuestTheme
import com.yibopan.quizquest.viewmodel.StatsUIState
import com.yibopan.quizquest.viewmodel.StatsViewModel
import com.yibopan.quizquest.views.components.AppTopBar
import com.yibopan.quizquest.views.components.LoadingState
import com.yibopan.quizquest.views.components.StarRow
import org.koin.compose.viewmodel.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** User statistics: totals, accuracy per world, last 7 days, recent results. */
@Composable
fun StatsScreen(
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
) {
    val statsViewModel: StatsViewModel = koinViewModel()
    val statsUIState by statsViewModel.uiState.collectAsStateWithLifecycle()
    StatsScreenContent(
        uiState = statsUIState,
        windowSizeClass = windowSizeClass,
    )
}

/**
 * Stateless Stats UI.
 * Lecture 3: Uses a two-pane layout on tablets/foldables (width != COMPACT).
 */
@Composable
fun StatsScreenContent(
    uiState: StatsUIState,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
) {
    val isTwoPane = windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT
    val summary = uiState.summary
    val categoryNameMap = remember(summary.byCategory) {
        summary.byCategory.associate { it.categoryId to it.categoryName }
    }

    Scaffold(
        modifier = modifier.testTag(TestTags.SCREEN_STATS),
        topBar = { AppTopBar(title = stringResource(R.string.stats_title)) },
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingState(modifier = Modifier.padding(paddingValues))
        } else if (isTwoPane) {
            // Lecture 3: Two-pane layout for wider screens (summary left, details right)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Left pane: Summary overview & Last 7 Days Activity
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item { OverviewCard(summary = summary) }
                    if (summary.last7Days.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.stats_last_7_days_header),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.semantics { heading() },
                            )
                        }
                        item { Last7DaysCard(activities = summary.last7Days) }
                    }
                }

                // Right pane: Accuracy per World & Recent Attempts
                LazyColumn(
                    modifier = Modifier.weight(1.2f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (summary.byCategory.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.stats_accuracy_header),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.semantics { heading() },
                            )
                        }
                        items(summary.byCategory, key = { it.categoryId }) { item ->
                            CategoryAccuracyRow(item = item)
                        }
                    }

                    if (summary.recentResults.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.stats_recent_results_header),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.semantics { heading() },
                            )
                        }
                        items(summary.recentResults, key = { it.sessionId }) { result ->
                            RecentResultRow(result = result, categoryNameMap = categoryNameMap)
                        }
                    }
                }
            }
        } else {
            // Single column layout for compact phone screens
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { OverviewCard(summary = summary) }
                if (summary.byCategory.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.stats_accuracy_header),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.semantics { heading() },
                        )
                    }
                    items(summary.byCategory, key = { it.categoryId }) { item ->
                        CategoryAccuracyRow(item = item)
                    }
                }
                if (summary.last7Days.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.stats_last_7_days_header),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.semantics { heading() },
                        )
                    }
                    item { Last7DaysCard(activities = summary.last7Days) }
                }
                if (summary.recentResults.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.stats_recent_results_header),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.semantics { heading() },
                        )
                    }
                    items(summary.recentResults, key = { it.sessionId }) { result ->
                        RecentResultRow(result = result, categoryNameMap = categoryNameMap)
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewCard(summary: StatsSummary) {
    val accuracyPercent = (summary.overallAccuracy * 100).toInt()
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .semantics(mergeDescendants = true) {},
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.stats_overview_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.stats_total_answered, summary.totalAnswered),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(R.string.stats_accuracy, accuracyPercent),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(R.string.stats_total_stars, summary.totalStars),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(R.string.stats_active_days, summary.activeDaysThisWeek),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun CategoryAccuracyRow(item: CategoryAccuracy) {
    val percent = (item.accuracy * 100).toInt()
    val talkBackState = stringResource(
        R.string.stats_accuracy_item_state,
        item.categoryName,
        percent,
        item.correct,
        item.answered,
    )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { stateDescription = talkBackState },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = item.categoryName, style = MaterialTheme.typography.titleSmall)
                Text(text = "$percent%", style = MaterialTheme.typography.bodySmall)
            }
            LinearProgressIndicator(
                progress = { item.accuracy },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun Last7DaysCard(activities: List<DailyActivity>) {
    val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val maxCount = (activities.maxOfOrNull { it.questionsAnswered } ?: 1).coerceAtLeast(1)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                activities.forEach { activity ->
                    val dayName = dateFormat.format(Date(activity.dayStartMillis))
                    val talkBackDesc = stringResource(
                        R.string.stats_daily_item_state,
                        dayName,
                        activity.questionsAnswered,
                    )
                    val fraction = activity.questionsAnswered.toFloat() / maxCount.toFloat()

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .semantics(mergeDescendants = true) { stateDescription = talkBackDesc },
                    ) {
                        Text(
                            text = "${activity.questionsAnswered}",
                            style = MaterialTheme.typography.labelSmall,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(48.dp),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentResultRow(result: QuizResult, categoryNameMap: Map<Int, String> = emptyMap()) {
    val worldName = categoryNameMap[result.categoryId]
        ?: stringResource(R.string.stats_unknown_world, result.categoryId)
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.stats_recent_item_desc, worldName, result.level, result.correctCount, result.totalCount),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            StarRow(stars = result.stars)
        }
    }
}

@Preview(name = "Phone", showBackground = true)
@Preview(name = "Tablet", device = "spec:width=1280dp,height=800dp,dpi=240", showBackground = true)
@Preview(name = "FontScale 200%", fontScale = 2f, showBackground = true)
@Preview(name = "Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun StatsScreenPreview() {
    QuizQuestTheme { StatsScreenContent(uiState = StatsUIState()) }
}
