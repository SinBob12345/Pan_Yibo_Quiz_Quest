package com.yibopan.quizquest.views

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yibopan.quizquest.R
import com.yibopan.quizquest.ui.theme.QuizQuestTheme
import com.yibopan.quizquest.viewmodel.PrivacyMessage
import com.yibopan.quizquest.viewmodel.PrivacyUIState
import com.yibopan.quizquest.viewmodel.PrivacyViewModel
import com.yibopan.quizquest.views.components.AppTopBar
import org.koin.compose.viewmodel.koinViewModel

/**
 * "Privacy & your data" screen (A2 commitments #2 and #4; Lecture 9: data safety & user control).
 * Handles JSON data export via system share sheet and data deletion.
 */
@Composable
fun PrivacyScreen(onBackPressed: () -> Unit) {
    val privacyViewModel: PrivacyViewModel = koinViewModel()
    val privacyUIState by privacyViewModel.uiState.collectAsStateWithLifecycle()
    PrivacyScreenContent(
        uiState = privacyUIState,
        onBackPressed = onBackPressed,
        onExportClicked = { privacyViewModel.prepareExport() },
        onExportHandled = { privacyViewModel.clearExportJson() },
        onRequestDeleteClicked = { privacyViewModel.requestDelete() },
        onConfirmDeleteClicked = { privacyViewModel.confirmDelete() },
        onDismissDeleteClicked = { privacyViewModel.dismissDelete() },
        onMessageShown = { privacyViewModel.messageShown() },
    )
}

@Composable
fun PrivacyScreenContent(
    uiState: PrivacyUIState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    onExportClicked: () -> Unit = {},
    onExportHandled: () -> Unit = {},
    onRequestDeleteClicked: () -> Unit = {},
    onConfirmDeleteClicked: () -> Unit = {},
    onDismissDeleteClicked: () -> Unit = {},
    onMessageShown: () -> Unit = {},
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val exportJson = uiState.exportJson
    LaunchedEffect(exportJson) {
        if (exportJson != null) {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, exportJson)
                type = "application/json"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            context.startActivity(shareIntent)
            onExportHandled()
        }
    }

    val message = uiState.message
    val exportReadyMsg = "Data export generated"
    val deletedMsg = "All data deleted successfully"
    val failedMsg = "Action failed"

    LaunchedEffect(message) {
        if (message != null) {
            val text = when (message) {
                PrivacyMessage.EXPORT_READY -> exportReadyMsg
                PrivacyMessage.DELETED -> deletedMsg
                PrivacyMessage.FAILED -> failedMsg
            }
            snackbarHostState.showSnackbar(text)
            onMessageShown()
        }
    }

    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = onDismissDeleteClicked,
            title = { Text(text = "Delete all data?") },
            text = { Text(text = "This will permanently delete all your level scores, review flashcards, and settings from this device. This action cannot be undone.") },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismissDeleteClicked,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(R.string.quiz_leave_dialog_dismiss))
                    }
                    OutlinedButton(
                        onClick = onConfirmDeleteClicked,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = "Delete")
                    }
                }
            },
        )
    }

    Scaffold(
        modifier = modifier.testTag(TestTags.SCREEN_PRIVACY),
        topBar = { AppTopBar(title = stringResource(R.string.privacy_title), onBackPressed = onBackPressed) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Data Safety Notice",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.semantics { heading() },
                        )
                        Text(
                            text = "QuizQuest stores all your progress, settings, and review flashcards locally on this device in Room database. No analytics, tracking, or personal data is collected or sent to external servers.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            item { HorizontalDivider() }

            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(text = "Export My Data", style = MaterialTheme.typography.titleSmall)
                            Text(text = "Share or save a JSON copy of all your scores and flashcards", style = MaterialTheme.typography.bodySmall)
                        }
                        OutlinedButton(onClick = onExportClicked, enabled = !uiState.isWorking) {
                            Icon(imageVector = Icons.Rounded.Download, contentDescription = null)
                            Text(text = "Export", modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }

            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(text = "Delete All Data", style = MaterialTheme.typography.titleSmall)
                            Text(text = "Permanently clear all database tables on this device", style = MaterialTheme.typography.bodySmall)
                        }
                        OutlinedButton(onClick = onRequestDeleteClicked, enabled = !uiState.isWorking) {
                            Icon(imageVector = Icons.Rounded.DeleteForever, contentDescription = null)
                            Text(text = "Delete", modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PrivacyScreenPreview() {
    QuizQuestTheme { PrivacyScreenContent(uiState = PrivacyUIState(), onBackPressed = {}) }
}
