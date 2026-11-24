package com.yibopan.quizquest.viewmodel

import com.yibopan.quizquest.data.model.RetentionPeriod
import com.yibopan.quizquest.data.model.UserSettings
import com.yibopan.quizquest.fakes.FakeExportWriter
import com.yibopan.quizquest.fakes.FakePrivacyRepository
import com.yibopan.quizquest.fakes.FakeSettingsRepository
import com.yibopan.quizquest.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrivacyViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var privacyRepo: FakePrivacyRepository
    private lateinit var settingsRepo: FakeSettingsRepository
    private lateinit var exportWriter: FakeExportWriter
    private lateinit var viewModel: PrivacyViewModel

    @Before
    fun setUp() {
        privacyRepo = FakePrivacyRepository()
        settingsRepo = FakeSettingsRepository()
        exportWriter = FakeExportWriter()
        viewModel = PrivacyViewModel(privacyRepo, settingsRepo, exportWriter, timeProvider = { 1000L })
    }

    @Test
    fun `fresh UserSettings defaults answerRetention to DAYS_30`() {
        val settings = UserSettings()
        assertEquals(RetentionPeriod.DAYS_30, settings.answerRetention)
    }

    @Test
    fun `reportedCount is observed and updated in uiState`() = runTest {
        privacyRepo.reportedCountFlow.value = 3

        val viewModel = PrivacyViewModel(privacyRepo, settingsRepo, exportWriter, timeProvider = { 1000L })

        assertEquals(3, viewModel.uiState.value.reportedCount)
    }

    @Test
    fun `updateAnswerRetention saves setting and calls deleteExpiredAnswers`() = runTest {
        viewModel.updateAnswerRetention(RetentionPeriod.DAYS_90)

        val settings = settingsRepo.getSettings().first()
        assertEquals(RetentionPeriod.DAYS_90, settings.answerRetention)
        assertEquals(1, privacyRepo.deleteExpiredAnswersCount)
    }

    @Test
    fun `saveExport writes json from repository and sets EXPORT_SAVED`() = runTest {
        val testUri = "content://com.android.providers.downloads/document/1"
        viewModel.saveExport(testUri)

        val state = viewModel.uiState.value
        assertFalse(state.isWorking)
        assertEquals(testUri, exportWriter.writtenUri)
        assertEquals("{\"data\": \"export\"}", exportWriter.writtenJson)
        assertEquals(PrivacyMessage.EXPORT_SAVED, state.message)
    }

    @Test
    fun `saveExport sets FAILED message and restores isWorking on Exception`() = runTest {
        exportWriter.shouldFail = true
        val testUri = "content://com.android.providers.downloads/document/1"

        viewModel.saveExport(testUri)

        val state = viewModel.uiState.value
        assertFalse(state.isWorking)
        assertEquals(PrivacyMessage.FAILED, state.message)
    }

    @Test
    fun `confirmDelete deletes data and updates state`() = runTest {
        viewModel.requestDelete()
        assertTrue(viewModel.uiState.value.showDeleteConfirmation)

        viewModel.confirmDelete()

        val state = viewModel.uiState.value
        assertFalse(state.showDeleteConfirmation)
        assertFalse(state.isWorking)
        assertTrue(privacyRepo.deleteAllDataCalled)
        assertEquals(PrivacyMessage.DELETED, state.message)
    }

    @Test
    fun `confirmRestore restores reported questions and sets RESTORED message`() = runTest {
        privacyRepo.reportedCountFlow.value = 2

        viewModel.requestRestore()
        assertTrue(viewModel.uiState.value.showRestoreConfirmation)

        viewModel.confirmRestore()

        val state = viewModel.uiState.value
        assertFalse(state.showRestoreConfirmation)
        assertFalse(state.isWorking)
        assertTrue(privacyRepo.restoreReportedQuestionsCalled)
        assertEquals(0, state.reportedCount)
        assertEquals(PrivacyMessage.RESTORED, state.message)
    }

    @Test
    fun `confirmRestore sets FAILED message on Exception`() = runTest {
        privacyRepo.shouldFail = true

        viewModel.requestRestore()
        viewModel.confirmRestore()

        val state = viewModel.uiState.value
        assertFalse(state.isWorking)
        assertEquals(PrivacyMessage.FAILED, state.message)
    }
}
