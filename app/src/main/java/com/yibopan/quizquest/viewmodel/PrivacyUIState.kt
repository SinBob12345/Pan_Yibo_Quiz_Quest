package com.yibopan.quizquest.viewmodel

enum class PrivacyMessage {
    EXPORT_SAVED,
    DELETED,
    FAILED,
}

/** Everything the Privacy & Your Data screen shows. */
data class PrivacyUIState(
    val isWorking: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val message: PrivacyMessage? = null,
)
