package com.yibopan.quizquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yibopan.quizquest.data.export.ExportWriter
import com.yibopan.quizquest.data.repository.PrivacyRepository
import com.yibopan.quizquest.util.AppLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Privacy & Data Safety screen (A2 commitments #2 and #4; Lecture 9).
 */
class PrivacyViewModel(
    private val privacyRepository: PrivacyRepository,
    private val exportWriter: ExportWriter,
) : ViewModel() {

    private companion object {
        private const val TAG = "PrivacyViewModel"
    }

    private val _uiState = MutableStateFlow(PrivacyUIState())
    val uiState: StateFlow<PrivacyUIState> get() = _uiState

    fun saveExport(uriString: String) {
        if (_uiState.value.isWorking) return
        _uiState.update { it.copy(isWorking = true, message = null) }

        viewModelScope.launch {
            try {
                val json = privacyRepository.exportAllDataAsJson()
                exportWriter.write(uriString, json)
                AppLog.d(TAG, "Data export successfully saved to $uriString")
                _uiState.update { it.copy(message = PrivacyMessage.EXPORT_SAVED) }
            } catch (e: Exception) {
                AppLog.e(TAG, "Failed to save data export to $uriString", e)
                _uiState.update { it.copy(message = PrivacyMessage.FAILED) }
            } finally {
                _uiState.update { it.copy(isWorking = false) }
            }
        }
    }

    fun requestDelete() {
        _uiState.update { it.copy(showDeleteConfirmation = true) }
    }

    fun dismissDelete() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }

    fun confirmDelete() {
        if (_uiState.value.isWorking) return
        _uiState.update { it.copy(showDeleteConfirmation = false, isWorking = true, message = null) }

        viewModelScope.launch {
            try {
                privacyRepository.deleteAllData()
                AppLog.d(TAG, "All data successfully deleted")
                _uiState.update { it.copy(message = PrivacyMessage.DELETED) }
            } catch (e: Exception) {
                AppLog.e(TAG, "Failed to delete data", e)
                _uiState.update { it.copy(message = PrivacyMessage.FAILED) }
            } finally {
                _uiState.update { it.copy(isWorking = false) }
            }
        }
    }

    fun messageShown() {
        _uiState.update { it.copy(message = null) }
    }
}
