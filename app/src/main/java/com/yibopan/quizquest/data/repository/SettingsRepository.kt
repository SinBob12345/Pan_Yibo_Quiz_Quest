package com.yibopan.quizquest.data.repository

import com.yibopan.quizquest.data.model.UserSettings
import kotlinx.coroutines.flow.Flow

/** Learner preferences, stored as one row in Room (Lecture 7). Implemented in Stage 4. */
interface SettingsRepository {

    /** Current settings; emits UserSettings() defaults until something is saved. */
    suspend fun getSettings(): Flow<UserSettings>

    suspend fun updateSettings(settings: UserSettings)
}
