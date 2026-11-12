package com.yibopan.quizquest.viewmodel

import com.yibopan.quizquest.data.model.StatsSummary

/** Everything the User Statistics screen shows. */
data class StatsUIState(
    val isLoading: Boolean = false,
    val summary: StatsSummary = StatsSummary(),
    val error: String? = null,
)
