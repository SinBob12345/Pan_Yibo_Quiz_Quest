package com.yibopan.quizquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yibopan.quizquest.data.model.SampleData
import com.yibopan.quizquest.data.repository.ProgressRepository
import com.yibopan.quizquest.data.repository.TriviaRepository
import com.yibopan.quizquest.data.rules.LevelPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel for the World map screen (Lecture 4).
 *
 * Collects categories and stored level progress to build the 9 level map tiles,
 * using [LevelPolicy] for difficulties and level unlocking.
 */
class WorldMapViewModel(
    private val categoryId: Int,
    private val progressRepository: ProgressRepository,
    private val triviaRepository: TriviaRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        WorldMapUIState(
            categoryId = categoryId,
            worldName = SampleData.worldName(categoryId),
            isLoading = true,
        ),
    )
    val uiState: StateFlow<WorldMapUIState> get() = _uiState

    init {
        loadWorldMap()
    }

    private fun loadWorldMap() {
        viewModelScope.launch {
            combine(
                triviaRepository.getCategories(),
                progressRepository.getLevelProgress(categoryId),
            ) { categories, progressList ->
                val categoryName = categories.find { it.id == categoryId }?.name
                    ?: SampleData.worldName(categoryId)

                val progressByLevel = progressList.associateBy { it.level }

                val levelItems = mutableListOf<LevelItem>()
                var previousStars: Int? = null

                for (level in 1..LevelPolicy.LEVELS_PER_WORLD) {
                    val difficulty = LevelPolicy.difficultyFor(level)
                    val isUnlocked = LevelPolicy.isUnlocked(level, previousStars)
                    val progress = progressByLevel[level]
                    val stars = progress?.stars ?: 0

                    levelItems.add(
                        LevelItem(
                            level = level,
                            difficulty = difficulty,
                            stars = stars,
                            unlocked = isUnlocked,
                        ),
                    )

                    previousStars = progress?.stars
                }

                WorldMapUIState(
                    categoryId = categoryId,
                    worldName = categoryName,
                    isLoading = false,
                    levels = levelItems,
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}
