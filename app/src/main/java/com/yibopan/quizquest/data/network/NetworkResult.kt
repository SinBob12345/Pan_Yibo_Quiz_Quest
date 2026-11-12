package com.yibopan.quizquest.data.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Result of a repository call (Lecture 5): either the data or an error message.
 * The error text is for developers/logs only — the UI always shows a friendly message
 * from strings.xml instead (Lecture 9: "Improve error messages").
 */
sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val error: String) : NetworkResult<Nothing>()
}

/** Wraps every value of a Flow in [NetworkResult.Success] and turns exceptions into [NetworkResult.Error] (Lecture 7). */
fun <T> Flow<T>.asResult(): Flow<NetworkResult<T>> {
    return this
        .map<T, NetworkResult<T>> {
            NetworkResult.Success(it)
        }
        .catch {
            emit(NetworkResult.Error(it.message.toString()))
        }
}
