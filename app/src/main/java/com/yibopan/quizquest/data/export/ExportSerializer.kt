package com.yibopan.quizquest.data.export

import com.yibopan.quizquest.data.model.AnswerRecord
import com.yibopan.quizquest.data.model.LevelProgress
import com.yibopan.quizquest.data.model.QuizResult
import com.yibopan.quizquest.data.model.ReviewCard
import com.yibopan.quizquest.data.model.UserSettings
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ReportedQuestion(
    val questionText: String,
    val reportedAt: Long,
)

/**
 * Export container for the learner's local data (A2 commitment #2, ACS Ethics 2.2(d)).
 * Contains strictly stored app data without device IDs, tracking codes, or personal identifiers.
 */
@Serializable
data class UserDataExport(
    val exportedAt: Long,
    val appVersion: String,
    val settings: UserSettings,
    val levelProgress: List<LevelProgress>,
    val sessions: List<QuizResult>,
    val answers: List<AnswerRecord>,
    val reviewCards: List<ReviewCard>,
    val reportedQuestions: List<ReportedQuestion>,
)

object ExportSerializer {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun toJson(export: UserDataExport): String {
        return json.encodeToString(UserDataExport.serializer(), export)
    }

    fun fromJson(jsonString: String): UserDataExport {
        return json.decodeFromString(UserDataExport.serializer(), jsonString)
    }
}
