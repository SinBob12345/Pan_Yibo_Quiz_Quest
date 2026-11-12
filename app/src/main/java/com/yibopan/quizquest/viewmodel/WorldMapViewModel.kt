package com.yibopan.quizquest.viewmodel

import androidx.lifecycle.ViewModel
import com.yibopan.quizquest.data.model.Difficulty
import com.yibopan.quizquest.data.model.SampleData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Stage 1: placeholder — all 9 levels unlocked, no stars, EASY.
 *
 * Stage 5A: inject ProgressRepository + TriviaRepository; collect getLevelProgress(categoryId)
 * and build the 9 LevelItems with LevelPolicy.difficultyFor / LevelPolicy.isUnlocked.
 */
class WorldMapViewModel(categoryId: Int) : ViewModel() {

    // Lecture 7: private MutableStateFlow + public read-only StateFlow.
    private val _uiState = MutableStateFlow(
        WorldMapUIState(
            categoryId = categoryId,
            worldName = SampleData.worldName(categoryId),
            levels = (1..PLACEHOLDER_LEVELS).map { level ->
                LevelItem(level = level, difficulty = Difficulty.EASY, stars = 0, unlocked = true)
            },
        ),
    )
    val uiState: StateFlow<WorldMapUIState> get() = _uiState

    private companion object {
        const val PLACEHOLDER_LEVELS = 9
    }
}
