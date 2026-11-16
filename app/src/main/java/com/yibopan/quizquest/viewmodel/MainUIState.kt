package com.yibopan.quizquest.viewmodel

import com.yibopan.quizquest.data.model.ThemeMode

/** State for [MainViewModel] holding app-level theme preferences. */
data class MainUIState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)
