package com.yibopan.quizquest.data.database

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Type converters for Room (Lecture 7: PetsTypeConverters).
 * Converts complex types (like List<String>) to and from JSON string representations.
 */
class QuizTypeConverters {

    @TypeConverter
    fun convertListToString(list: List<String>): String {
        return Json.encodeToString(list)
    }

    @TypeConverter
    fun convertStringToList(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        return try {
            Json.decodeFromString(text)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
