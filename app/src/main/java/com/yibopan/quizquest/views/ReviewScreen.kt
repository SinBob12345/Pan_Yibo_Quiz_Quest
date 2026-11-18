package com.yibopan.quizquest.views

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yibopan.quizquest.R
import com.yibopan.quizquest.data.model.ReviewCard
import com.yibopan.quizquest.ui.theme.QuizQuestTheme
import com.yibopan.quizquest.viewmodel.ReviewUIState
import com.yibopan.quizquest.viewmodel.ReviewViewModel
import com.yibopan.quizquest.views.components.AppTopBar
import com.yibopan.quizquest.views.components.LoadingState
import org.koin.compose.viewmodel.koinViewModel

/** Flashcard review of wrongly answered questions. */
@Composable
fun ReviewScreen() {
    val reviewViewModel: ReviewViewModel = koinViewModel()
    val reviewUIState by reviewViewModel.uiState.collectAsStateWithLifecycle()
    ReviewScreenContent(
        uiState = reviewUIState,
        onShowAnswer = { reviewViewModel.showAnswer() },
        onKnewIt = { reviewViewModel.onKnewIt() },
        onNotYet = { reviewViewModel.onNotYet() },
    )
}

@Composable
fun ReviewScreenContent(
    uiState: ReviewUIState,
    modifier: Modifier = Modifier,
    onShowAnswer: () -> Unit = {},
    onKnewIt: () -> Unit = {},
    onNotYet: () -> Unit = {},
) {
    val card = uiState.currentCard
    val showAnswerLabel = stringResource(R.string.click_label_show_answer)
    val knewItLabel = stringResource(R.string.click_label_knew_it)
    val notYetLabel = stringResource(R.string.click_label_not_yet)

    Scaffold(
        modifier = modifier.testTag(TestTags.SCREEN_REVIEW),
        topBar = { AppTopBar(title = stringResource(R.string.review_title)) },
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingState(modifier = Modifier.padding(paddingValues))
        } else if (card == null) {
            // All caught up / finished stopping cue card
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .semantics(mergeDescendants = true) {},
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.review_all_caught_up_title),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.semantics { heading() },
                            )
                            Text(
                                text = stringResource(R.string.review_all_caught_up_desc),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Header Bar: Progress & Leitner Box Label
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.review_card_progress, uiState.currentIndex + 1, uiState.dueCards.size),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = stringResource(R.string.review_box_label, card.box),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    // Flashcard Question Side
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = card.questionText,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.semantics { heading() },
                            )

                            // Lecture 5: AnimatedVisibility reveals the answer
                            AnimatedVisibility(visible = uiState.isAnswerRevealed) {
                                Column {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = card.correctAnswer,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }

                    // Action Controls: "Show answer" or Equal-weight ("Not yet" / "I knew it")
                    if (!uiState.isAnswerRevealed) {
                        OutlinedButton(
                            onClick = onShowAnswer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    onClick(label = showAnswerLabel, action = null)
                                },
                        ) {
                            Text(text = stringResource(R.string.review_show_answer))
                        }
                    } else {
                        // Equal visual weight buttons (A2 Commitment #5)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            OutlinedButton(
                                onClick = onNotYet,
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics {
                                        onClick(label = notYetLabel, action = null)
                                    },
                            ) {
                                Text(text = stringResource(R.string.review_not_yet))
                            }
                            OutlinedButton(
                                onClick = onKnewIt,
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics {
                                        onClick(label = knewItLabel, action = null)
                                    },
                            ) {
                                Text(text = stringResource(R.string.review_knew_it))
                            }
                        }
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
fun ReviewScreenPreview() {
    QuizQuestTheme {
        ReviewScreenContent(
            uiState = ReviewUIState(
                dueCards = listOf(
                    ReviewCard(1L, "What is H2O?", "Water", 1, 1000L, 1),
                ),
                isAnswerRevealed = true,
            ),
        )
    }
}
