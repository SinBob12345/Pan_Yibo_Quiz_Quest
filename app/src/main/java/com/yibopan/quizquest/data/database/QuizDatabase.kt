package com.yibopan.quizquest.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Room database for QuizQuest (Lecture 7: CatDatabase).
 *
 * Version 1 includes all 8 entity tables:
 * Category, Question, FlaggedQuestion, LevelProgress, QuizSession, AnswerRecord, ReviewCard, Settings.
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
    version = 1,
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
