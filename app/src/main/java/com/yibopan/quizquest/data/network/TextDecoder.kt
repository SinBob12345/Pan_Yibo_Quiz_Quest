package com.yibopan.quizquest.data.network

/**
 * Stage 2 — decodes strings returned with `encode=url3986`.
 *
 * RFC 3986 encodes a space as "%20" and a literal "+" as "%2B". java.net.URLDecoder would turn
 * "+" into a space, so replace "+" with "%2B" first, then decode with UTF-8. Must handle
 * multi-byte characters (e.g. "%C3%A9" -> "é").
 */
object TextDecoder {

    fun decodeUrl3986(encoded: String): String = TODO("Stage 2")
}
