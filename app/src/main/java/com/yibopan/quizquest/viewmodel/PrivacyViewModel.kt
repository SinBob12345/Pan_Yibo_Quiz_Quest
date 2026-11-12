package com.yibopan.quizquest.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Stage 1: placeholder.
 *
 * Stage 7: inject PrivacyRepository; prepareExport() puts the JSON in the state for the share sheet;
 * requestDelete() / confirmDelete() / dismissDelete() / messageShown().
 */
class PrivacyViewModel : ViewModel() {

    // Lecture 7: private MutableStateFlow + public read-only StateFlow.
    private val _uiState = MutableStateFlow(PrivacyUIState())
    val uiState: StateFlow<PrivacyUIState> get() = _uiState
}
