package com.yibopan.quizquest.viewmodel

import com.yibopan.quizquest.data.repository.PrivacyRepository
import com.yibopan.quizquest.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Fake PrivacyRepository for unit testing [PrivacyViewModel]. */
class FakePrivacyRepository : PrivacyRepository {
    var exportJsonResult: String = "{\"exportedAt\": 1000}"
    var deleteAllDataCalled = false
    var shouldFail = false

    override suspend fun exportAllDataAsJson(): String {
        if (shouldFail) throw IllegalStateException("Export failed")
        return exportJsonResult
    }

    override suspend fun deleteAllData() {
        if (shouldFail) throw IllegalStateException("Delete failed")
        deleteAllDataCalled = true
    }

    override suspend fun deleteExpiredAnswers(nowMillis: Long): Int = 0
}

@OptIn(ExperimentalCoroutinesApi::class)
class PrivacyViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var privacyRepo: FakePrivacyRepository
    private lateinit var viewModel: PrivacyViewModel

    @Before
    fun setUp() {
        privacyRepo = FakePrivacyRepository()
        viewModel = PrivacyViewModel(privacyRepo)
    }

    @Test
    fun `prepareExport loads json and updates state`() = runTest {
        viewModel.prepareExport()

        val state = viewModel.uiState.value
        assertFalse(state.isWorking)
        assertNotNull(state.exportJson)
        assertEquals(PrivacyMessage.EXPORT_READY, state.message)
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
}
