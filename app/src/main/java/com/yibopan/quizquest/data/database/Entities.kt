package com.yibopan.quizquest.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yibopan.quizquest.data.model.AnswerRecord
import com.yibopan.quizquest.data.model.Category
import com.yibopan.quizquest.data.model.Difficulty
import com.yibopan.quizquest.data.model.LevelProgress
import com.yibopan.quizquest.data.model.Question
import com.yibopan.quizquest.data.model.QuestionType
import com.yibopan.quizquest.data.model.QuizResult
import com.yibopan.quizquest.data.model.RetentionPeriod
import com.yibopan.quizquest.data.model.ReviewCard
import com.yibopan.quizquest.data.model.ThemeMode
import com.yibopan.quizquest.data.model.TimerMode
import com.yibopan.quizquest.data.model.UserSettings

/**
 * Room database entities (Lecture 7).
 * Table definitions follow AGENTS.md > Data model.
 */

@Entity(tableName = "Category")
data class CategoryEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val updatedAt: Long,
)

@Entity(
    tableName = "Question",
    indices = [Index(value = ["categoryId", "text"], unique = true)],
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val categoryId: Int,
    val difficulty: String,
    val type: String,
    val text: String,
    val correctAnswer: String,
    val incorrectAnswers: List<String>,
    val fetchedAt: Long,
    val used: Boolean = false,
)

@Entity(tableName = "FlaggedQuestion")
data class FlaggedQuestionEntity(
    @PrimaryKey val questionText: String,
    val flaggedAt: Long,
)

@Entity(
    tableName = "LevelProgress",
    primaryKeys = ["categoryId", "level"],
)
data class LevelProgressEntity(
    val categoryId: Int,
    val level: Int,
    val stars: Int,
    val bestScore: Int,
    val updatedAt: Long,
)

@Entity(tableName = "QuizSession")
data class QuizSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val categoryId: Int,
    val level: Int,
    val startedAt: Long,
    val durationMs: Long,
    val correctCount: Int,
    val totalCount: Int,
    val stars: Int,
)

@Entity(
    tableName = "AnswerRecord",
    foreignKeys = [
        ForeignKey(
            entity = QuizSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["sessionId"])],
)
data class AnswerRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionId: Long,
    val questionText: String,
    val chosenAnswer: String?,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val answeredAt: Long,
    @ColumnInfo(defaultValue = "0") val timeTakenMs: Long = 0L,
)

@Entity(
    tableName = "ReviewCard",
    indices = [Index(value = ["questionText"], unique = true)],
)
data class ReviewCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val questionText: String,
    val correctAnswer: String,
    val box: Int,
    val nextReviewAt: Long,
    val timesWrong: Int,
    val createdAt: Long,
)

@Entity(tableName = "Settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 0,
    val questionsPerLevel: Int = 10,
    val timerMode: String = "OFF",
    val soundEnabled: Boolean = true,
    val reduceMotion: Boolean = false,
    val themeMode: String = "SYSTEM",
    val dailyGoalLevels: Int = 3,
    val answerRetention: String = "DAYS_90",
)

// ----- Entity <-> Domain Model Mappers -----

fun CategoryEntity.toCategory(): Category = Category(id = id, name = name)
fun Category.toEntity(updatedAt: Long): CategoryEntity = CategoryEntity(id = id, name = name, updatedAt = updatedAt)

fun QuestionEntity.toQuestion(): Question = Question(
    id = id,
    categoryId = categoryId,
    difficulty = Difficulty.fromApi(difficulty),
    type = QuestionType.fromApi(type),
    text = text,
    correctAnswer = correctAnswer,
    incorrectAnswers = incorrectAnswers,
)

fun Question.toEntity(fetchedAt: Long, used: Boolean = false): QuestionEntity = QuestionEntity(
    id = id,
    categoryId = categoryId,
    difficulty = difficulty.apiValue,
    type = type.apiValue,
    text = text,
    correctAnswer = correctAnswer,
    incorrectAnswers = incorrectAnswers,
    fetchedAt = fetchedAt,
    used = used,
)

fun FlaggedQuestionEntity.toQuestionText(): String = questionText
fun String.toFlaggedEntity(flaggedAt: Long): FlaggedQuestionEntity = FlaggedQuestionEntity(questionText = this, flaggedAt = flaggedAt)

fun LevelProgressEntity.toLevelProgress(): LevelProgress = LevelProgress(
    categoryId = categoryId,
    level = level,
    stars = stars,
    bestScore = bestScore,
)

fun LevelProgress.toEntity(updatedAt: Long): LevelProgressEntity = LevelProgressEntity(
    categoryId = categoryId,
    level = level,
    stars = stars,
    bestScore = bestScore,
    updatedAt = updatedAt,
)

fun QuizSessionEntity.toQuizResult(): QuizResult = QuizResult(
    sessionId = id,
    categoryId = categoryId,
    level = level,
    correctCount = correctCount,
    totalCount = totalCount,
    stars = stars,
    startedAt = startedAt,
    durationMs = durationMs,
)

fun QuizResult.toEntity(): QuizSessionEntity = QuizSessionEntity(
    id = sessionId,
    categoryId = categoryId,
    level = level,
    startedAt = startedAt,
    durationMs = durationMs,
    correctCount = correctCount,
    totalCount = totalCount,
    stars = stars,
)

fun AnswerRecordEntity.toAnswerRecord(): AnswerRecord = AnswerRecord(
    questionText = questionText,
    chosenAnswer = chosenAnswer,
    correctAnswer = correctAnswer,
    isCorrect = isCorrect,
    timeTakenMs = timeTakenMs,
)

fun AnswerRecord.toEntity(sessionId: Long, answeredAt: Long): AnswerRecordEntity = AnswerRecordEntity(
    sessionId = sessionId,
    questionText = questionText,
    chosenAnswer = chosenAnswer,
    correctAnswer = correctAnswer,
    isCorrect = isCorrect,
    answeredAt = answeredAt,
    timeTakenMs = timeTakenMs,
)

fun ReviewCardEntity.toReviewCard(): ReviewCard = ReviewCard(
    id = id,
    questionText = questionText,
    correctAnswer = correctAnswer,
    box = box,
    nextReviewAt = nextReviewAt,
    timesWrong = timesWrong,
)

fun ReviewCard.toEntity(createdAt: Long): ReviewCardEntity = ReviewCardEntity(
    id = id,
    questionText = questionText,
    correctAnswer = correctAnswer,
    box = box,
    nextReviewAt = nextReviewAt,
    timesWrong = timesWrong,
    createdAt = createdAt,
)

fun SettingsEntity.toUserSettings(): UserSettings = UserSettings(
    questionsPerLevel = questionsPerLevel,
    timerMode = try { TimerMode.valueOf(timerMode) } catch (_: Exception) { TimerMode.OFF },
    soundEnabled = soundEnabled,
    reduceMotion = reduceMotion,
    themeMode = try { ThemeMode.valueOf(themeMode) } catch (_: Exception) { ThemeMode.SYSTEM },
    dailyGoalLevels = dailyGoalLevels,
    answerRetention = try { RetentionPeriod.valueOf(answerRetention) } catch (_: Exception) { RetentionPeriod.DAYS_90 },
)

fun UserSettings.toEntity(): SettingsEntity = SettingsEntity(
    id = 0,
    questionsPerLevel = questionsPerLevel,
    timerMode = timerMode.name,
    soundEnabled = soundEnabled,
    reduceMotion = reduceMotion,
    themeMode = themeMode.name,
    dailyGoalLevels = dailyGoalLevels,
    answerRetention = answerRetention.name,
)
