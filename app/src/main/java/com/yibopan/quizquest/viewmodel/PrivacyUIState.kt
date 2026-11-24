package com.yibopan.quizquest.viewmodel

import com.yibopan.quizquest.data.model.UserSettings

enum class PrivacyMessage {
    EXPORT_SAVED,
    DELETED,
    RESTORED,
    FAILED,
}

/** Everything the Privacy & Your Data screen shows. */
data class PrivacyUIState(
    val isWorking: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val showRestoreConfirmation: Boolean = false,
    val reportedCount: Int = 0,
    val settings: UserSettings = UserSettings(),
    val message: PrivacyMessage? = null,
)
