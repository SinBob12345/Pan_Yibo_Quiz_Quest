package com.yibopan.quizquest.data.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for [QuizTypeConverters] JSON serialization and deserialization. */
class QuizTypeConvertersTest {

    private val converters = QuizTypeConverters()

    @Test
    fun `convertListToString encodes string list to JSON`() {
        val list = listOf("Paris", "London", "Berlin")
        val json = converters.convertListToString(list)

        assertEquals("[\"Paris\",\"London\",\"Berlin\"]", json)
    }

    @Test
    fun `convertStringToList decodes JSON to string list`() {
        val json = "[\"Paris\",\"London\",\"Berlin\"]"
        val list = converters.convertStringToList(json)

        assertEquals(listOf("Paris", "London", "Berlin"), list)
    }

    @Test
    fun `convertStringToList handles empty or blank string`() {
        assertTrue(converters.convertStringToList("").isEmpty())
        assertTrue(converters.convertStringToList("   ").isEmpty())
    }

    @Test
    fun `convertListToString handles empty list`() {
        val json = converters.convertListToString(emptyList())
        assertEquals("[]", json)
    }

    @Test
    fun `convertStringToList handles malformed JSON gracefully`() {
        val list = converters.convertStringToList("invalid_json")
        assertTrue(list.isEmpty())
    }
}
