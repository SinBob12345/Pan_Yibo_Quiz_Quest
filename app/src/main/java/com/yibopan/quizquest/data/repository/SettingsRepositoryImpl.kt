package com.yibopan.quizquest.data.repository

import com.yibopan.quizquest.data.database.SettingsDao
import com.yibopan.quizquest.data.database.toEntity
import com.yibopan.quizquest.data.database.toUserSettings
import com.yibopan.quizquest.data.model.UserSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Learner preferences repository stored in Room (Lecture 7).
 *
 * Maps the single SettingsEntity row to UserSettings; missing row uses UserSettings() defaults.
 */
class SettingsRepositoryImpl(
    private val settingsDao: SettingsDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SettingsRepository {

    override suspend fun getSettings(): Flow<UserSettings> {
        return settingsDao.getSettings()
            .map { entity -> entity?.toUserSettings() ?: UserSettings() }
    }

    override suspend fun updateSettings(settings: UserSettings): Unit = withContext(dispatcher) {
        settingsDao.insertOrUpdate(settings.toEntity())
    }
}
