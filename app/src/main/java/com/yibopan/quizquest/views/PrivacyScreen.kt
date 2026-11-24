package com.yibopan.quizquest.views

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yibopan.quizquest.R
import com.yibopan.quizquest.data.model.RetentionPeriod
import com.yibopan.quizquest.ui.theme.QuizQuestTheme
import com.yibopan.quizquest.viewmodel.PrivacyMessage
import com.yibopan.quizquest.viewmodel.PrivacyUIState
import com.yibopan.quizquest.viewmodel.PrivacyViewModel
import com.yibopan.quizquest.views.components.AppTopBar
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * "Privacy & your data" screen (A2 commitments #2 and #4; Lecture 9: data safety & user control).
 * Displays plain-language data policies, attribution (CC BY-SA 4.0), retention controls, file export, deletion, and email feedback.
 */
@Composable
fun PrivacyScreen(onBackPressed: () -> Unit) {
    val privacyViewModel: PrivacyViewModel = koinViewModel()
    val privacyUIState by privacyViewModel.uiState.collectAsStateWithLifecycle()
    PrivacyScreenContent(
        uiState = privacyUIState,
        onBackPressed = onBackPressed,
        onAnswerRetentionChanged = { privacyViewModel.updateAnswerRetention(it) },
        onSaveExport = { uriString -> privacyViewModel.saveExport(uriString) },
        onRequestDeleteClicked = { privacyViewModel.requestDelete() },
        onConfirmDeleteClicked = { privacyViewModel.confirmDelete() },
        onDismissDeleteClicked = { privacyViewModel.dismissDelete() },
        onRequestRestoreClicked = { privacyViewModel.requestRestore() },
        onConfirmRestoreClicked = { privacyViewModel.confirmRestore() },
        onDismissRestoreClicked = { privacyViewModel.dismissRestore() },
        onMessageShown = { privacyViewModel.messageShown() },
    )
}

@Composable
fun PrivacyScreenContent(
    uiState: PrivacyUIState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    onAnswerRetentionChanged: (RetentionPeriod) -> Unit = {},
    onSaveExport: (String) -> Unit = {},
    onRequestDeleteClicked: () -> Unit = {},
    onConfirmDeleteClicked: () -> Unit = {},
    onDismissDeleteClicked: () -> Unit = {},
    onRequestRestoreClicked: () -> Unit = {},
    onConfirmRestoreClicked: () -> Unit = {},
    onDismissRestoreClicked: () -> Unit = {},
    onMessageShown: () -> Unit = {},
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showRetentionDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            onSaveExport(uri.toString())
        }
    }

    val feedbackEmail = stringResource(R.string.feedback_email)
    val feedbackSubject = stringResource(R.string.feedback_subject)
    val noEmailAppMsg = stringResource(R.string.privacy_no_email_app_snackbar)

    val message = uiState.message
    val exportSavedMsg = stringResource(R.string.privacy_snackbar_export_saved)
    val deletedMsg = stringResource(R.string.privacy_snackbar_deleted)
    val restoredMsg = stringResource(R.string.privacy_snackbar_restored)
    val failedMsg = stringResource(R.string.privacy_snackbar_failed)

    LaunchedEffect(message) {
        if (message != null) {
            val text = when (message) {
                PrivacyMessage.EXPORT_SAVED -> exportSavedMsg
                PrivacyMessage.DELETED -> deletedMsg
                PrivacyMessage.RESTORED -> restoredMsg
                PrivacyMessage.FAILED -> failedMsg
            }
            snackbarHostState.showSnackbar(text)
            onMessageShown()
        }
    }

    // Answer Retention Option Dialog
    if (showRetentionDialog) {
        ChoiceDialog(
            title = stringResource(R.string.settings_retention_title),
            options = RetentionPeriod.entries,
            selectedOption = uiState.settings.answerRetention,
            optionLabel = { period ->
                when (period) {
                    RetentionPeriod.DAYS_30 -> stringResource(R.string.settings_retention_30)
                    RetentionPeriod.DAYS_90 -> stringResource(R.string.settings_retention_90)
                    RetentionPeriod.FOREVER -> stringResource(R.string.settings_retention_forever)
                }
            },
            onOptionSelected = {
                onAnswerRetentionChanged(it)
                showRetentionDialog = false
            },
            onDismiss = { showRetentionDialog = false },
        )
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

    // Restore Reported Questions AlertDialog (ACS Conduct 1.2.2(d): undoing as easy as doing)
    if (uiState.showRestoreConfirmation) {
        AlertDialog(
            onDismissRequest = onDismissRestoreClicked,
            title = { Text(text = stringResource(R.string.privacy_restore_dialog_title), modifier = Modifier.semantics { heading() }) },
            text = { Text(text = stringResource(R.string.privacy_restore_dialog_text)) },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismissRestoreClicked,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp),
                    ) {
                        Text(text = stringResource(R.string.common_cancel))
                    }
                    OutlinedButton(
                        onClick = onConfirmRestoreClicked,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp),
                    ) {
                        Text(text = stringResource(R.string.privacy_restore_dialog_confirm))
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

                            // Answer Retention Control directly under "How long we keep it"
                            val retentionText = when (uiState.settings.answerRetention) {
                                RetentionPeriod.DAYS_30 -> stringResource(R.string.settings_retention_30)
                                RetentionPeriod.DAYS_90 -> stringResource(R.string.settings_retention_90)
                                RetentionPeriod.FOREVER -> stringResource(R.string.settings_retention_forever)
                            }
                            OutlinedButton(
                                onClick = { showRetentionDialog = true },
                                enabled = !uiState.isWorking,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp),
                            ) {
                                Text(text = stringResource(R.string.settings_retention_title) + ": " + retentionText)
                            }

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

                // Reported questions restore card
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
                                Text(text = stringResource(R.string.privacy_reported_title), style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = pluralStringResource(R.plurals.privacy_reported_questions_count, uiState.reportedCount, uiState.reportedCount),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            OutlinedButton(
                                onClick = onRequestRestoreClicked,
                                enabled = !uiState.isWorking && uiState.reportedCount > 0,
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                            ) {
                                Icon(imageVector = Icons.Rounded.Restore, contentDescription = null)
                                Text(text = stringResource(R.string.privacy_restore_button), modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    }
                }

                item { HorizontalDivider() }

                // Save a Copy / Export File Action
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
                                onClick = {
                                    val todayDate = dateFormat.format(Date())
                                    launcher.launch("quizquest-export-$todayDate.json")
                                },
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
                                    sendFeedbackEmail(
                                        context = context,
                                        email = feedbackEmail,
                                        subject = feedbackSubject,
                                        onNoEmailApp = {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(noEmailAppMsg)
                                            }
                                        },
                                    )
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

private fun sendFeedbackEmail(
    context: Context,
    email: String,
    subject: String,
    onNoEmailApp: () -> Unit,
) {
    val mailUri = "mailto:$email".toUri()
    val sendToIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = mailUri
        putExtra(Intent.EXTRA_SUBJECT, subject)
    }

    try {
        context.startActivity(sendToIntent)
    } catch (_: Exception) {
        val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        try {
            context.startActivity(Intent.createChooser(fallbackIntent, "Send email"))
        } catch (_: Exception) {
            onNoEmailApp()
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
