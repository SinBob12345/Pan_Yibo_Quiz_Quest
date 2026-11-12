package com.yibopan.quizquest.viewmodel

/** One-off feedback shown after an export or delete. */
enum class PrivacyMessage {
    EXPORT_READY,
    DELETED,
    FAILED,
}

/** Everything the "Privacy & your data" screen shows. */
data class PrivacyUIState(
    val isWorking: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    /** JSON to hand to the share sheet; null when there is nothing to share. */
    val exportJson: String? = null,
    val message: PrivacyMessage? = null,
)
