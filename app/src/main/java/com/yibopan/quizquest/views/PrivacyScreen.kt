package com.yibopan.quizquest.views

import android.content.Intent
import android.content.res.Configuration
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Email
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
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
 * Displays plain-language data policies, attribution (CC BY-SA 4.0), export, deletion, and email feedback.
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
    val exportReadyMsg = stringResource(R.string.privacy_snackbar_export_ready)
    val deletedMsg = stringResource(R.string.privacy_snackbar_deleted)
    val failedMsg = stringResource(R.string.privacy_snackbar_failed)

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

    // Neutral Delete All Data AlertDialog with equal visual weight buttons (A2 Commitment #5)
    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = onDismissDeleteClicked,
            title = { Text(text = stringResource(R.string.privacy_delete_dialog_title), modifier = Modifier.semantics { heading() }) },
            text = { Text(text = stringResource(R.string.privacy_delete_dialog_text)) },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismissDeleteClicked,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp),
                    ) {
                        Text(text = stringResource(R.string.privacy_delete_keep))
                    }
                    OutlinedButton(
                        onClick = onConfirmDeleteClicked,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp),
                    ) {
                        Text(text = stringResource(R.string.privacy_delete_confirm))
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
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Plain language privacy sections
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            PrivacySection(
                                title = stringResource(R.string.privacy_what_is_stored_title),
                                desc = stringResource(R.string.privacy_what_is_stored_desc),
                            )
                            PrivacySection(
                                title = stringResource(R.string.privacy_retention_title),
                                desc = stringResource(R.string.privacy_retention_desc),
                            )
                            PrivacySection(
                                title = stringResource(R.string.privacy_never_collected_title),
                                desc = stringResource(R.string.privacy_never_collected_desc),
                            )
                            PrivacySection(
                                title = stringResource(R.string.privacy_opentdb_title),
                                desc = stringResource(R.string.privacy_opentdb_desc),
                            )
                            PrivacySection(
                                title = stringResource(R.string.privacy_backups_title),
                                desc = stringResource(R.string.privacy_backups_desc),
                            )
                            PrivacySection(
                                title = stringResource(R.string.privacy_mistakes_title),
                                desc = stringResource(R.string.privacy_mistakes_desc),
                            )
                            PrivacySection(
                                title = stringResource(R.string.privacy_attribution_title),
                                desc = stringResource(R.string.privacy_attribution_desc),
                            )
                        }
                    }
                }

                item { HorizontalDivider() }

                // Export My Data Action
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
                                Text(text = stringResource(R.string.privacy_export_title), style = MaterialTheme.typography.titleSmall)
                                Text(text = stringResource(R.string.privacy_export_desc), style = MaterialTheme.typography.bodySmall)
                            }
                            OutlinedButton(
                                onClick = onExportClicked,
                                enabled = !uiState.isWorking,
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                            ) {
                                Icon(imageVector = Icons.Rounded.Download, contentDescription = null)
                                Text(text = stringResource(R.string.privacy_export_button), modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    }
                }

                // Delete All Data Action
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
                                Text(text = stringResource(R.string.privacy_delete_title), style = MaterialTheme.typography.titleSmall)
                                Text(text = stringResource(R.string.privacy_delete_desc), style = MaterialTheme.typography.bodySmall)
                            }
                            OutlinedButton(
                                onClick = onRequestDeleteClicked,
                                enabled = !uiState.isWorking,
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                            ) {
                                Icon(imageVector = Icons.Rounded.DeleteForever, contentDescription = null)
                                Text(text = stringResource(R.string.privacy_delete_button), modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    }
                }

                item { HorizontalDivider() }

                // Lecture 9: Send Feedback Email Channel
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
                                Text(text = stringResource(R.string.privacy_feedback_title), style = MaterialTheme.typography.titleSmall)
                                Text(text = stringResource(R.string.privacy_feedback_desc), style = MaterialTheme.typography.bodySmall)
                            }
                            OutlinedButton(
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = "mailto:support@quizquest.edu.au".toUri()
                                        putExtra(Intent.EXTRA_SUBJECT, "QuizQuest Feedback")
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        val fallback = Intent(Intent.ACTION_SEND).apply {
                                            type = "message/rfc822"
                                            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@quizquest.edu.au"))
                                            putExtra(Intent.EXTRA_SUBJECT, "QuizQuest Feedback")
                                        }
                                        try {
                                            context.startActivity(Intent.createChooser(fallback, "Send email"))
                                        } catch (_: Exception) { }
                                    }
                                },
                            ) {
                                Icon(imageVector = Icons.Rounded.Email, contentDescription = null)
                                Text(text = stringResource(R.string.privacy_send_feedback), modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacySection(title: String, desc: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview(name = "Phone", showBackground = true)
@Preview(name = "FontScale 200%", fontScale = 2f, showBackground = true)
@Preview(name = "Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PrivacyScreenPreview() {
    QuizQuestTheme { PrivacyScreenContent(uiState = PrivacyUIState(), onBackPressed = {}) }
}
