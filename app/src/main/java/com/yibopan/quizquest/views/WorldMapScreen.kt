package com.yibopan.quizquest.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yibopan.quizquest.R
import com.yibopan.quizquest.data.model.Difficulty
import com.yibopan.quizquest.ui.theme.QuizQuestTheme
import com.yibopan.quizquest.viewmodel.LevelItem
import com.yibopan.quizquest.viewmodel.WorldMapUIState
import com.yibopan.quizquest.viewmodel.WorldMapViewModel
import com.yibopan.quizquest.views.components.AppTopBar
import com.yibopan.quizquest.views.components.StageNotice
import com.yibopan.quizquest.views.components.StarRow
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Level map of one world. [categoryId] comes from the navigation argument (Lecture 6). */
@Composable
fun WorldMapScreen(
    categoryId: Int,
    onBackPressed: () -> Unit,
    onLevelClicked: (level: Int) -> Unit,
) {
    val worldMapViewModel: WorldMapViewModel = koinViewModel(parameters = { parametersOf(categoryId) })
    val worldMapUIState by worldMapViewModel.uiState.collectAsStateWithLifecycle()
    WorldMapScreenContent(
        uiState = worldMapUIState,
        onBackPressed = onBackPressed,
        onLevelClicked = onLevelClicked,
    )
}

/** Stateless World map UI. Lecture 2: GridCells.Adaptive adds columns on wider screens. */
@Composable
fun WorldMapScreenContent(
    uiState: WorldMapUIState,
    onBackPressed: () -> Unit,
    onLevelClicked: (level: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.testTag(TestTags.SCREEN_WORLD_MAP),
        topBar = {
            AppTopBar(
                title = uiState.worldName.ifBlank { stringResource(R.string.world_map_title) },
                onBackPressed = onBackPressed,
            )
        },
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 104.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                StageNotice(stage = 5, description = stringResource(R.string.dev_world_map_stage))
            }
            items(uiState.levels, key = { it.level }) { level ->
                LevelTile(level = level, onClick = { onLevelClicked(level.level) })
            }
        }
    }
}

@Composable
private fun LevelTile(level: LevelItem, onClick: () -> Unit) {
    // Lecture 3: describe the state ("Locked" / "2 of 3 stars") and label the click action.
    val state = if (level.unlocked) {
        stringResource(R.string.world_level_stars_state, level.stars)
    } else {
        stringResource(R.string.world_level_locked_state)
    }
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { stateDescription = state }
            .clickable(
                enabled = level.unlocked,
                onClickLabel = stringResource(R.string.click_label_start_level),
                onClick = onClick,
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.world_level_label, level.level),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(text = difficultyLabel(level.difficulty), style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(6.dp))
            if (level.unlocked) {
                StarRow(stars = level.stars)
            } else {
                Icon(imageVector = Icons.Rounded.Lock, contentDescription = null)
            }
        }
    }
}

@Composable
private fun difficultyLabel(difficulty: Difficulty): String = when (difficulty) {
    Difficulty.EASY -> stringResource(R.string.difficulty_easy)
    Difficulty.MEDIUM -> stringResource(R.string.difficulty_medium)
    Difficulty.HARD -> stringResource(R.string.difficulty_hard)
}

@Preview(showBackground = true)
@Composable
fun WorldMapScreenPreview() {
    QuizQuestTheme {
        WorldMapScreenContent(
            uiState = WorldMapUIState(
                categoryId = 9,
                worldName = "General Knowledge",
                levels = listOf(
                    LevelItem(level = 1, difficulty = Difficulty.EASY, stars = 3, unlocked = true),
                    LevelItem(level = 2, difficulty = Difficulty.EASY, stars = 1, unlocked = true),
                    LevelItem(level = 3, difficulty = Difficulty.EASY, stars = 0, unlocked = false),
                ),
            ),
            onBackPressed = {},
            onLevelClicked = {},
        )
    }
}
