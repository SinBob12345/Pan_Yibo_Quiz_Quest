package com.yibopan.quizquest.viewmodel

import com.yibopan.quizquest.data.model.UserSettings

/** Everything the Settings screen shows. */
data class SettingsUIState(
    val settings: UserSettings = UserSettings(),
)
