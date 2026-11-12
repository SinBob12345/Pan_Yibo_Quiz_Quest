package com.yibopan.quizquest.data.repository

// Stage 7.
//
// class PrivacyRepositoryImpl(database, dao(s), settingsRepository, timeProvider, dispatcher) : PrivacyRepository
// deleteAllData(): database.clearAllTables()
// deleteExpiredAnswers(): RetentionPolicy.cutoffMillis + answerDao.deleteOlderThan(cutoff)
