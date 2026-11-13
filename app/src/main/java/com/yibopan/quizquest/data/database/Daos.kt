package com.yibopan.quizquest.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Room Data Access Objects (Lecture 7: CatDao). */

@Dao
interface CategoryDao {
    @Query("SELECT * FROM Category ORDER BY id ASC")
    fun getCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM Category")
    suspend fun deleteAll()
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM Question WHERE categoryId = :categoryId AND difficulty = :difficulty AND text NOT IN (SELECT questionText FROM FlaggedQuestion) AND used = 0 LIMIT :amount")
    suspend fun getUnusedQuestions(categoryId: Int, difficulty: String, amount: Int): List<QuestionEntity>

    @Query("SELECT * FROM Question WHERE categoryId = :categoryId AND difficulty = :difficulty AND text NOT IN (SELECT questionText FROM FlaggedQuestion) LIMIT :amount")
    suspend fun getQuestions(categoryId: Int, difficulty: String, amount: Int): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Query("UPDATE Question SET used = 1 WHERE id IN (:ids)")
    suspend fun markAsUsed(ids: List<Long>)

    @Query("DELETE FROM Question")
    suspend fun deleteAll()
}

@Dao
interface FlaggedQuestionDao {
    @Query("SELECT questionText FROM FlaggedQuestion")
    fun getFlaggedQuestionTexts(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flaggedQuestion: FlaggedQuestionEntity)

    @Query("DELETE FROM FlaggedQuestion WHERE questionText = :questionText")
    suspend fun delete(questionText: String)
}

@Dao
interface LevelProgressDao {
    @Query("SELECT * FROM LevelProgress WHERE categoryId = :categoryId")
    fun getLevelProgressForCategory(categoryId: Int): Flow<List<LevelProgressEntity>>

    @Query("SELECT * FROM LevelProgress WHERE categoryId = :categoryId AND level = :level")
    suspend fun getProgress(categoryId: Int, level: Int): LevelProgressEntity?

    @Query("SELECT * FROM LevelProgress")
    fun getAllProgress(): Flow<List<LevelProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: LevelProgressEntity)

    @Query("DELETE FROM LevelProgress")
    suspend fun deleteAll()
}

@Dao
interface QuizSessionDao {
    @Query("SELECT * FROM QuizSession ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<QuizSessionEntity>>

    @Query("SELECT * FROM QuizSession WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): QuizSessionEntity?

    @Query("SELECT startedAt FROM QuizSession WHERE correctCount > 0 ORDER BY startedAt DESC")
    fun getSessionStartTimes(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: QuizSessionEntity): Long

    @Query("DELETE FROM QuizSession")
    suspend fun deleteAll()
}

@Dao
interface AnswerRecordDao {
    @Query("SELECT * FROM AnswerRecord WHERE sessionId = :sessionId")
    suspend fun getAnswersForSession(sessionId: Long): List<AnswerRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(answers: List<AnswerRecordEntity>)

    @Query("DELETE FROM AnswerRecord WHERE answeredAt < :cutoffMillis")
    suspend fun deleteOlderThan(cutoffMillis: Long): Int

    @Query("DELETE FROM AnswerRecord")
    suspend fun deleteAll()
}

@Dao
interface ReviewCardDao {
    @Query("SELECT * FROM ReviewCard ORDER BY nextReviewAt ASC")
    fun getAllReviewCards(): Flow<List<ReviewCardEntity>>

    @Query("SELECT * FROM ReviewCard WHERE nextReviewAt <= :nowMillis ORDER BY nextReviewAt ASC")
    fun getDueReviewCards(nowMillis: Long): Flow<List<ReviewCardEntity>>

    @Query("SELECT * FROM ReviewCard WHERE questionText = :questionText LIMIT 1")
    suspend fun getCardByQuestionText(questionText: String): ReviewCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: ReviewCardEntity): Long

    @Update
    suspend fun update(card: ReviewCardEntity)

    @Query("DELETE FROM ReviewCard WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ReviewCard")
    suspend fun deleteAll()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM Settings WHERE id = 0")
    fun getSettings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM Settings WHERE id = 0")
    suspend fun getSettingsDirect(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: SettingsEntity)

    @Query("DELETE FROM Settings")
    suspend fun deleteAll()
}
