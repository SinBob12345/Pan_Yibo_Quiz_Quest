package com.yibopan.quizquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yibopan.quizquest.data.repository.PrivacyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Privacy & Your Data screen (Lecture 4).
 *
 * Handles export of all learner data as JSON and deletion of database records.
 */
class PrivacyViewModel(
    private val privacyRepository: PrivacyRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrivacyUIState())
    val uiState: StateFlow<PrivacyUIState> get() = _uiState

    fun prepareExport() {
        if (_uiState.value.isWorking) return
        _uiState.update { it.copy(isWorking = true, message = null) }

        viewModelScope.launch {
            try {
                val json = privacyRepository.exportAllDataAsJson()
                _uiState.update {
                    it.copy(
                        isWorking = false,
                        exportJson = json,
                        message = PrivacyMessage.EXPORT_READY,
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isWorking = false,
                        message = PrivacyMessage.FAILED,
                    )
                }
            }
        }
    }

    fun clearExportJson() {
        _uiState.update { it.copy(exportJson = null) }
    }

    fun requestDelete() {
        _uiState.update { it.copy(showDeleteConfirmation = true) }
    }

    fun dismissDelete() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }

    fun confirmDelete() {
        if (_uiState.value.isWorking) return
        _uiState.update { it.copy(isWorking = true, showDeleteConfirmation = false) }

        viewModelScope.launch {
            try {
                privacyRepository.deleteAllData()
                _uiState.update {
                    it.copy(
                        isWorking = false,
                        message = PrivacyMessage.DELETED,
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isWorking = false,
                        message = PrivacyMessage.FAILED,
                    )
                }
            }
        }
    }

    fun messageShown() {
        _uiState.update { it.copy(message = null) }
    }
}
