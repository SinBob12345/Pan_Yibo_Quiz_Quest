package com.yibopan.quizquest.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yibopan.quizquest.data.database.FlaggedQuestionEntity
import com.yibopan.quizquest.data.database.QuizDatabase
import com.yibopan.quizquest.data.database.SettingsEntity
import com.yibopan.quizquest.data.export.ExportSerializer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Integration tests for [PrivacyRepositoryImpl] export and deletion (A2 Commitment #2). */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class PrivacyRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var db: QuizDatabase
    private lateinit var repository: PrivacyRepositoryImpl

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            QuizDatabase::class.java,
        ).allowMainThreadQueries().build()

        repository = PrivacyRepositoryImpl(
            database = db,
            settingsDao = db.settingsDao(),
            levelProgressDao = db.levelProgressDao(),
            quizSessionDao = db.quizSessionDao(),
            answerRecordDao = db.answerRecordDao(),
            reviewCardDao = db.reviewCardDao(),
            flaggedQuestionDao = db.flaggedQuestionDao(),
            appVersion = "1.0.0",
            timeProvider = { 1000L },
            dispatcher = testDispatcher,
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun exportAllDataAsJsonIncludesAppVersionAndReportedQuestions() = runBlocking {
        db.settingsDao().insertOrUpdate(SettingsEntity(id = 0, questionsPerLevel = 10, timerMode = "OFF", soundEnabled = true, reduceMotion = false, themeMode = "SYSTEM", dailyGoalLevels = 3, answerRetention = "DAYS_90"))
        db.flaggedQuestionDao().insert(FlaggedQuestionEntity("Inappropriate Q", 5000L))

        val jsonString = repository.exportAllDataAsJson()
        val export = ExportSerializer.fromJson(jsonString)

        assertEquals("1.0.0", export.appVersion)
        assertNotNull(export.reportedQuestions)
        assertEquals(1, export.reportedQuestions.size)
        assertEquals("Inappropriate Q", export.reportedQuestions[0].questionText)
        assertEquals(5000L, export.reportedQuestions[0].reportedAt)
    }
}
