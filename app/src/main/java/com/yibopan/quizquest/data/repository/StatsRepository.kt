package com.yibopan.quizquest.data.repository

import com.yibopan.quizquest.data.model.StatsSummary
import kotlinx.coroutines.flow.Flow

/** Read-only learning statistics built from Room queries. Implemented in Stage 4. */
interface StatsRepository {
    suspend fun getStatsSummary(): Flow<StatsSummary>
}
