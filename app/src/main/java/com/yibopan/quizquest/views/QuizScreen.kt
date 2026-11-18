package com.yibopan.quizquest.views

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yibopan.quizquest.R
import com.yibopan.quizquest.ui.theme.QuizQuestTheme
import com.yibopan.quizquest.viewmodel.QuizUIState
import com.yibopan.quizquest.viewmodel.QuizViewModel
import com.yibopan.quizquest.views.components.AppTopBar
import com.yibopan.quizquest.views.components.ErrorState
import com.yibopan.quizquest.views.components.LoadingState
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The main learning activity ("Activity Screen" in the brief).
 *
 * Uses AnimatedVisibility for loading, error, and content states (Lecture 5).
 * Respects reduceMotion for all progress, question, and feedback animations (A2 Commitment #7).
 */
@Composable
fun QuizScreen(
    categoryId: Int,
    level: Int,
    onBackPressed: () -> Unit,
    onQuizFinished: (sessionId: Long) -> Unit,
) {
    val quizViewModel: QuizViewModel = koinViewModel(parameters = { parametersOf(categoryId, level) })
    val quizUIState by quizViewModel.uiState.collectAsStateWithLifecycle()

    val finishedSessionId = quizUIState.finishedSessionId
    LaunchedEffect(finishedSessionId) {
        if (finishedSessionId != null) {
            onQuizFinished(finishedSessionId)
        }
    }

    QuizScreenContent(
        uiState = quizUIState,
        onBackPressed = onBackPressed,
        onSelectAnswer = { quizViewModel.selectAnswer(it) },
        onFiftyFiftyClicked = { quizViewModel.useFiftyFifty() },
        onReportQuestionClicked = { quizViewModel.flagCurrentQuestion() },
        onNextClicked = { quizViewModel.nextQuestion() },
        onRetryClicked = { quizViewModel.retry() },
    )
}

@Composable
fun QuizScreenContent(
    uiState: QuizUIState,
    onBackPressed: () -> Unit,
    onSelectAnswer: (String) -> Unit,
    onFiftyFiftyClicked: () -> Unit,
    modifier: Modifier = Modifier,
    onReportQuestionClicked: () -> Unit = {},
    onNextClicked: () -> Unit = {},
    onRetryClicked: () -> Unit = {},
) {
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    val isQuizInProgress = uiState.questionNumber > 0 && !uiState.isLoading && uiState.error == null && uiState.finishedSessionId == null

    // Catch system back gesture / back button when a quiz level is in progress
    BackHandler(enabled = isQuizInProgress) {
        showLeaveDialog = true
    }

    // Neutral Leave AlertDialog with equal visual weight buttons (A2 Commitment #5)
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text(text = stringResource(R.string.quiz_leave_dialog_title)) },
            text = { Text(text = stringResource(R.string.quiz_leave_dialog_message)) },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = { showLeaveDialog = false },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(R.string.quiz_leave_dialog_dismiss))
                    }
                    OutlinedButton(
                        onClick = {
                            showLeaveDialog = false
                            onBackPressed()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(R.string.quiz_leave_dialog_confirm))
                    }
                }
            },
        )
    }

    // Neutral Report Question AlertDialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(text = stringResource(R.string.quiz_report_question_title)) },
            text = { Text(text = stringResource(R.string.quiz_report_question_message)) },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = { showReportDialog = false },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(R.string.quiz_leave_dialog_dismiss))
                    }
                    OutlinedButton(
                        onClick = {
                            showReportDialog = false
                            onReportQuestionClicked()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(R.string.quiz_report_dialog_confirm))
                    }
                }
            },
        )
    }

    val handleBackPress = {
        if (isQuizInProgress) {
            showLeaveDialog = true
        } else {
            onBackPressed()
        }
    }

    val hintLabel = stringResource(R.string.click_label_use_hint)
    val selectLabel = stringResource(R.string.click_label_select_answer)
    val nextLabel = stringResource(R.string.click_label_next_question)

    val targetProgress = if (uiState.totalQuestions > 0) {
        uiState.questionNumber.toFloat() / uiState.totalQuestions.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = if (uiState.reduceMotion) snap() else tween(durationMillis = 300),
        label = "quizProgress",
    )

    Scaffold(
        modifier = modifier.testTag(TestTags.SCREEN_QUIZ),
        topBar = {
            AppTopBar(
                title = stringResource(R.string.quiz_title, uiState.level),
                onBackPressed = handleBackPress,
                actions = {
                    if (isQuizInProgress) {
                        IconButton(onClick = { showReportDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Flag,
                                contentDescription = stringResource(R.string.cd_report_question),
                            )
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Lecture 5: AnimatedVisibility for loading state
            AnimatedVisibility(
                visible = uiState.isLoading,
                enter = if (uiState.reduceMotion) fadeIn(animationSpec = snap()) else fadeIn(animationSpec = tween(200)),
                exit = if (uiState.reduceMotion) fadeOut(animationSpec = snap()) else fadeOut(animationSpec = tween(200)),
            ) {
                LoadingState()
            }

            // Lecture 5 & Lecture 9: AnimatedVisibility for friendly error state with retry
            AnimatedVisibility(
                visible = !uiState.isLoading && uiState.error != null,
                enter = if (uiState.reduceMotion) fadeIn(animationSpec = snap()) else fadeIn(animationSpec = tween(200)),
                exit = if (uiState.reduceMotion) fadeOut(animationSpec = snap()) else fadeOut(animationSpec = tween(200)),
            ) {
                ErrorState(
                    message = stringResource(R.string.quiz_error_friendly),
                    onRetry = onRetryClicked,
                )
            }

            // Lecture 5: AnimatedVisibility for question content state
            AnimatedVisibility(
                visible = !uiState.isLoading && uiState.error == null && uiState.totalQuestions > 0,
                enter = if (uiState.reduceMotion) fadeIn(animationSpec = snap()) else fadeIn(animationSpec = tween(200)),
                exit = if (uiState.reduceMotion) fadeOut(animationSpec = snap()) else fadeOut(animationSpec = tween(200)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Header Bar: Progress, Timer, 50/50 Hint Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.quiz_progress, uiState.questionNumber, uiState.totalQuestions),
                            style = MaterialTheme.typography.titleSmall,
                        )

                        if (uiState.secondsLeft != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = stringResource(R.string.quiz_seconds_left, uiState.secondsLeft),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onFiftyFiftyClicked,
                            enabled = uiState.selectedAnswer == null && uiState.hintsLeft > 0,
                            modifier = Modifier.semantics {
                                onClick(label = hintLabel, action = null)
                            },
                        ) {
                            Text(text = stringResource(R.string.quiz_hint_fifty_fifty, uiState.hintsLeft))
                        }
                    }

                    // AnimatedContent between questions
                    AnimatedContent(
                        targetState = uiState.questionNumber,
                        transitionSpec = {
                            if (uiState.reduceMotion) {
                                fadeIn(animationSpec = snap()) togetherWith fadeOut(animationSpec = snap())
                            } else {
                                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                            }
                        },
                        label = "questionContent",
                    ) { qNum ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.testTag("question_$qNum"),
                        ) {
                            // Question Text
                            Text(
                                text = uiState.questionText,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .semantics { heading() },
                            )

                            // Options Buttons List
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                uiState.options.forEach { option ->
                                    if (option !in uiState.removedOptions) {
                                        val isSelected = (uiState.selectedAnswer == option)
                                        val isCorrectOption = (uiState.correctAnswer == option)
                                        val isWrongSelection = (isSelected && uiState.correctAnswer != null && !isCorrectOption)

                                        val containerColor = when {
                                            uiState.correctAnswer != null && isCorrectOption -> MaterialTheme.colorScheme.primaryContainer
                                            isWrongSelection -> MaterialTheme.colorScheme.errorContainer
                                            isSelected -> MaterialTheme.colorScheme.surfaceVariant
                                            else -> MaterialTheme.colorScheme.surface
                                        }

                                        Button(
                                            onClick = { onSelectAnswer(option) },
                                            enabled = uiState.selectedAnswer == null,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = containerColor,
                                                contentColor = when {
                                                    uiState.correctAnswer != null && isCorrectOption -> MaterialTheme.colorScheme.onPrimaryContainer
                                                    isWrongSelection -> MaterialTheme.colorScheme.onErrorContainer
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                },
                                                disabledContainerColor = containerColor,
                                                disabledContentColor = when {
                                                    uiState.correctAnswer != null && isCorrectOption -> MaterialTheme.colorScheme.onPrimaryContainer
                                                    isWrongSelection -> MaterialTheme.colorScheme.onErrorContainer
                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .semantics {
                                                    onClick(label = selectLabel, action = null)
                                                },
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Text(
                                                    text = option,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    modifier = Modifier.weight(1f),
                                                )

                                                // Lecture 3: Feedback uses an icon AND text (never colour alone)
                                                if (uiState.correctAnswer != null) {
                                                    if (isCorrectOption) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.CheckCircle,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                        )
                                                    } else if (isWrongSelection) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Close,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.error,
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Answer Feedback & Next Question Action with short scale/fade
                    AnimatedVisibility(
                        visible = uiState.selectedAnswer != null,
                        enter = if (uiState.reduceMotion) fadeIn(animationSpec = snap()) else fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.95f, animationSpec = tween(200)),
                        exit = if (uiState.reduceMotion) fadeOut(animationSpec = snap()) else fadeOut(animationSpec = tween(200)),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            val isUserCorrect = (uiState.selectedAnswer == uiState.correctAnswer)

                            // Lecture 3: TalkBack announces feedback via liveRegion = Polite
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUserCorrect) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.errorContainer
                                    },
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics { liveRegion = LiveRegionMode.Polite },
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Icon(
                                        imageVector = if (isUserCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.Close,
                                        contentDescription = null,
                                        tint = if (isUserCorrect) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onErrorContainer
                                        },
                                    )
                                    Text(
                                        text = if (isUserCorrect) {
                                            stringResource(R.string.quiz_feedback_correct)
                                        } else {
                                            stringResource(R.string.quiz_feedback_incorrect, uiState.correctAnswer.orEmpty())
                                        },
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isUserCorrect) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onErrorContainer
                                        },
                                    )
                                }
                            }

                            Button(
                                onClick = onNextClicked,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics {
                                        onClick(label = nextLabel, action = null)
                                    },
                            ) {
                                Text(
                                    text = if (uiState.questionNumber >= uiState.totalQuestions) {
                                        stringResource(R.string.quiz_finish_quiz)
                                    } else {
                                        stringResource(R.string.quiz_next_question)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuizScreenPreview() {
    QuizQuestTheme {
        QuizScreenContent(
            uiState = QuizUIState(
                categoryId = 9,
                level = 1,
                questionNumber = 1,
                totalQuestions = 10,
                questionText = "What is the capital of France?",
                options = listOf("Paris", "London", "Berlin", "Rome"),
                selectedAnswer = "Paris",
                correctAnswer = "Paris",
                hintsLeft = 2,
            ),
            onBackPressed = {},
            onSelectAnswer = {},
            onFiftyFiftyClicked = {},
            onReportQuestionClicked = {},
            onNextClicked = {},
            onRetryClicked = {},
        )
    }
}
