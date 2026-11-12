package com.yibopan.quizquest.viewmodel

import com.yibopan.quizquest.data.model.Difficulty

/** One tile on the level map. */
data class LevelItem(
    val level: Int,
    val difficulty: Difficulty,
    val stars: Int,
    val unlocked: Boolean,
)

/** Everything the World map screen shows. */
data class WorldMapUIState(
    val categoryId: Int,
    val worldName: String = "",
    val isLoading: Boolean = false,
    val levels: List<LevelItem> = emptyList(),
)
