package com.yibopan.quizquest.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Room database for QuizQuest (Lecture 7: CatDatabase).
 *
 * Version 2 adds `timeTakenMs` column to [AnswerRecordEntity] via [AutoMigration] from 1 to 2.
 */
@Database(
    entities = [
        CategoryEntity::class,
        QuestionEntity::class,
        FlaggedQuestionEntity::class,
        LevelProgressEntity::class,
        QuizSessionEntity::class,
        AnswerRecordEntity::class,
        ReviewCardEntity::class,
        SettingsEntity::class,
    ],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
    ],
    exportSchema = true,
)
@TypeConverters(QuizTypeConverters::class)
abstract class QuizDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun questionDao(): QuestionDao
    abstract fun flaggedQuestionDao(): FlaggedQuestionDao
    abstract fun levelProgressDao(): LevelProgressDao
    abstract fun quizSessionDao(): QuizSessionDao
    abstract fun answerRecordDao(): AnswerRecordDao
    abstract fun reviewCardDao(): ReviewCardDao
    abstract fun settingsDao(): SettingsDao
}
