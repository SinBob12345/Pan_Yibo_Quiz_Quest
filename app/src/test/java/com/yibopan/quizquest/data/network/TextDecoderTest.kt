package com.yibopan.quizquest.data.network

import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for [TextDecoder] RFC 3986 URL decoding. */
class TextDecoderTest {

    @Test
    fun `decodes standard percent encoded spaces and symbols`() {
        assertEquals("Hello World", TextDecoder.decodeUrl3986("Hello%20World"))
        assertEquals("What is 1+1?", TextDecoder.decodeUrl3986("What%20is%201%2B1%3F"))
        assertEquals("5+5=10", TextDecoder.decodeUrl3986("5%2B5%3D10"))
    }

    @Test
    fun `preserves literal plus signs`() {
        assertEquals("C++", TextDecoder.decodeUrl3986("C++"))
        assertEquals("C++", TextDecoder.decodeUrl3986("C%2B%2B"))
        assertEquals("A+B=C", TextDecoder.decodeUrl3986("A+B=C"))
    }

    @Test
    fun `decodes multi-byte UTF-8 characters`() {
        assertEquals("é", TextDecoder.decodeUrl3986("%C3%A9"))
        assertEquals("été", TextDecoder.decodeUrl3986("%C3%A9t%C3%A9"))
        assertEquals("ñ", TextDecoder.decodeUrl3986("%C3%B1"))
        assertEquals("€", TextDecoder.decodeUrl3986("%E2%82%AC"))
    }

    @Test
    fun `handles empty and plain strings`() {
        assertEquals("", TextDecoder.decodeUrl3986(""))
        assertEquals("plain text", TextDecoder.decodeUrl3986("plain text"))
    }

    @Test
    fun `decodes quotes and punctuation`() {
        assertEquals("\"quoted\"", TextDecoder.decodeUrl3986("%22quoted%22"))
        assertEquals("it's", TextDecoder.decodeUrl3986("it%27s"))
        assertEquals("100%", TextDecoder.decodeUrl3986("100%25"))
    }
}
