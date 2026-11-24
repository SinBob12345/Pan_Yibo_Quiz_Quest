package com.yibopan.quizquest.views

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yibopan.quizquest.R
import com.yibopan.quizquest.data.model.ThemeMode
import com.yibopan.quizquest.data.model.TimerMode
import com.yibopan.quizquest.ui.theme.QuizQuestTheme
import com.yibopan.quizquest.viewmodel.SettingsUIState
import com.yibopan.quizquest.viewmodel.SettingsViewModel
import com.yibopan.quizquest.views.components.AppTopBar
import org.koin.compose.viewmodel.koinViewModel

/** Learner preferences + entry to "Privacy & your data". */
@Composable
fun SettingsScreen(onPrivacyClicked: () -> Unit) {
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settingsUIState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreenContent(
        uiState = settingsUIState,
        onPrivacyClicked = onPrivacyClicked,
        onSoundChanged = { settingsViewModel.updateSoundEnabled(it) },
        onReduceMotionChanged = { settingsViewModel.updateReduceMotion(it) },
        onQuestionsPerLevelChanged = { settingsViewModel.updateQuestionsPerLevel(it) },
        onTimerModeChanged = { settingsViewModel.updateTimerMode(it) },
        onDailyGoalChanged = { settingsViewModel.updateDailyGoal(it) },
        onThemeModeChanged = { settingsViewModel.updateThemeMode(it) },
    )
}

@Composable
fun SettingsScreenContent(
    uiState: SettingsUIState,
    onPrivacyClicked: () -> Unit,
    modifier: Modifier = Modifier,
    onSoundChanged: (Boolean) -> Unit = {},
    onReduceMotionChanged: (Boolean) -> Unit = {},
    onQuestionsPerLevelChanged: (Int) -> Unit = {},
    onTimerModeChanged: (TimerMode) -> Unit = {},
    onDailyGoalChanged: (Int) -> Unit = {},
    onThemeModeChanged: (ThemeMode) -> Unit = {},
) {
    val settings = uiState.settings

    var showQuestionsDialog by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }
    var showDailyGoalDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showQuestionsDialog) {
        ChoiceDialog(
            title = stringResource(R.string.settings_questions_per_level_title),
            options = listOf(5, 10, 15),
            selectedOption = settings.questionsPerLevel,
            optionLabel = { stringResource(R.string.settings_questions_per_level_desc, it) },
            onOptionSelected = {
                onQuestionsPerLevelChanged(it)
                showQuestionsDialog = false
            },
            onDismiss = { showQuestionsDialog = false },
        )
    }

    if (showTimerDialog) {
        ChoiceDialog(
            title = stringResource(R.string.settings_timer_title),
            options = TimerMode.entries,
            selectedOption = settings.timerMode,
            optionLabel = { mode ->
                when (mode) {
                    TimerMode.OFF -> stringResource(R.string.settings_timer_off)
                    TimerMode.RELAXED -> stringResource(R.string.settings_timer_relaxed)
                    TimerMode.CHALLENGE -> stringResource(R.string.settings_timer_challenge)
                }
            },
            onOptionSelected = {
                onTimerModeChanged(it)
                showTimerDialog = false
            },
            onDismiss = { showTimerDialog = false },
        )
    }

    if (showDailyGoalDialog) {
        ChoiceDialog(
            title = stringResource(R.string.settings_daily_goal_title),
            options = listOf(1, 2, 3, 5),
            selectedOption = settings.dailyGoalLevels,
            optionLabel = { stringResource(R.string.settings_daily_goal_desc, it) },
            onOptionSelected = {
                onDailyGoalChanged(it)
                showDailyGoalDialog = false
            },
            onDismiss = { showDailyGoalDialog = false },
        )
    }

    if (showThemeDialog) {
        ChoiceDialog(
            title = stringResource(R.string.settings_theme_title),
            options = ThemeMode.entries,
            selectedOption = settings.themeMode,
            optionLabel = { mode ->
                when (mode) {
                    ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
                    ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
                    ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
                }
            },
            onOptionSelected = {
                onThemeModeChanged(it)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false },
        )
    }

    Scaffold(
        modifier = modifier.testTag(TestTags.SCREEN_SETTINGS),
        topBar = { AppTopBar(title = stringResource(R.string.settings_title)) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                // Sound Effects Switch (Whole row toggleable for TalkBack)
                item {
                    ListItem(
                        headlineContent = { Text(text = stringResource(R.string.settings_sound_title)) },
                        leadingContent = { Icon(imageVector = Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null) },
                        trailingContent = {
                            Switch(checked = settings.soundEnabled, onCheckedChange = null)
                        },
                        modifier = Modifier.toggleable(
                            value = settings.soundEnabled,
                            onValueChange = onSoundChanged,
                            role = Role.Switch,
                        ),
                    )
                }

                // Reduce Motion Switch (Whole row toggleable for TalkBack)
                item {
                    ListItem(
                        headlineContent = { Text(text = stringResource(R.string.settings_reduce_motion_title)) },
                        supportingContent = { Text(text = stringResource(R.string.settings_reduce_motion_desc)) },
                        leadingContent = { Icon(imageVector = Icons.Rounded.Animation, contentDescription = null) },
                        trailingContent = {
                            Switch(checked = settings.reduceMotion, onCheckedChange = null)
                        },
                        modifier = Modifier.toggleable(
                            value = settings.reduceMotion,
                            onValueChange = onReduceMotionChanged,
                            role = Role.Switch,
                        ),
                    )
                }

                item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

                // Questions per level
                item {
                    ListItem(
                        headlineContent = { Text(text = stringResource(R.string.settings_questions_per_level_title)) },
                        supportingContent = {
                            Text(text = stringResource(R.string.settings_questions_per_level_desc, settings.questionsPerLevel))
                        },
                        leadingContent = { Icon(imageVector = Icons.Rounded.Quiz, contentDescription = null) },
                        modifier = Modifier.clickable(
                            onClickLabel = stringResource(R.string.click_label_open),
                            onClick = { showQuestionsDialog = true },
                        ),
                    )
                }

                // Timer mode
                item {
                    val timerText = when (settings.timerMode) {
                        TimerMode.OFF -> stringResource(R.string.settings_timer_off)
                        TimerMode.RELAXED -> stringResource(R.string.settings_timer_relaxed)
                        TimerMode.CHALLENGE -> stringResource(R.string.settings_timer_challenge)
                    }
                    ListItem(
                        headlineContent = { Text(text = stringResource(R.string.settings_timer_title)) },
                        supportingContent = { Text(text = timerText) },
                        leadingContent = { Icon(imageVector = Icons.Rounded.Speed, contentDescription = null) },
                        modifier = Modifier.clickable(
                            onClickLabel = stringResource(R.string.click_label_open),
                            onClick = { showTimerDialog = true },
                        ),
                    )
                }

                // Daily goal
                item {
                    ListItem(
                        headlineContent = { Text(text = stringResource(R.string.settings_daily_goal_title)) },
                        supportingContent = {
                            Text(text = stringResource(R.string.settings_daily_goal_desc, settings.dailyGoalLevels))
                        },
                        leadingContent = { Icon(imageVector = Icons.Rounded.Category, contentDescription = null) },
                        modifier = Modifier.clickable(
                            onClickLabel = stringResource(R.string.click_label_open),
                            onClick = { showDailyGoalDialog = true },
                        ),
                    )
                }

                // App theme
                item {
                    val themeText = when (settings.themeMode) {
                        ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
                        ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
                        ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
                    }
                    ListItem(
                        headlineContent = { Text(text = stringResource(R.string.settings_theme_title)) },
                        supportingContent = { Text(text = themeText) },
                        leadingContent = { Icon(imageVector = Icons.Rounded.DarkMode, contentDescription = null) },
                        modifier = Modifier.clickable(
                            onClickLabel = stringResource(R.string.click_label_open),
                            onClick = { showThemeDialog = true },
                        ),
                    )
                }

                item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

                // Privacy Entry
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
}

@Composable
private fun <T> ChoiceDialog(
    title: String,
    options: List<T>,
    selectedOption: T,
    optionLabel: @Composable (T) -> String,
    onOptionSelected: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, modifier = Modifier.semantics { heading() }) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .selectable(
                                selected = (option == selectedOption),
                                onClick = { onOptionSelected(option) },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RadioButton(
                            selected = (option == selectedOption),
                            onClick = null,
                        )
                        Text(
                            text = optionLabel(option),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cd_back))
            }
        },
    )
}

@Preview(name = "Phone", showBackground = true)
@Preview(name = "FontScale 200%", fontScale = 2f, showBackground = true)
@Preview(name = "Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun SettingsScreenPreview() {
    QuizQuestTheme { SettingsScreenContent(uiState = SettingsUIState(), onPrivacyClicked = {}) }
}
