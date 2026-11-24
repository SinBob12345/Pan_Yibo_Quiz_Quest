package com.yibopan.quizquest.viewmodel

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
    val message: PrivacyMessage? = null,
)
