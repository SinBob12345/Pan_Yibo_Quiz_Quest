package com.yibopan.quizquest.util

import android.util.Log
import com.yibopan.quizquest.BuildConfig

/**
 * Tiny logging helper (Lectures 8 and 9).
 * Only logs when [BuildConfig.DEBUG] is true.
 */
object AppLog {
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message, throwable)
        }
    }
}
