package com.yibopan.quizquest.data.repository

// Stage 2 (network) + Stage 4 (Room, offline first). Pattern: Lectures 5 and 7 PetsRepositoryImpl.
//
// class TriviaRepositoryImpl(
//     private val triviaAPI: TriviaAPI,
//     private val rateLimiter: RateLimiter,
//     private val dispatcher: CoroutineDispatcher,
//     // Stage 4: + categoryDao, questionDao
// ) : TriviaRepository
// - every function body: return withContext(dispatcher) { try { ... } catch (e: Exception) { ... } }
// - every API call goes through rateLimiter.throttle { }
// - check response.isSuccessful, then the Open Trivia response_code:
//   3 (token not found): request a new token and retry once
//   4 (token empty): reset the token and retry once
//   1 (no results) / 5 (rate limit): NetworkResult.Error(...)
// - offline (exception): use unused questions saved in Room (Stage 4), else NetworkResult.Error
