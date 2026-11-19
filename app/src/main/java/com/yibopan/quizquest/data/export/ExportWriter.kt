package com.yibopan.quizquest.data.export

import android.content.Context
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Interface for writing exported JSON data directly to a destination URI (Stage 8).
 */
interface ExportWriter {
    suspend fun write(uriString: String, json: String)
}

class AndroidExportWriter(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ExportWriter {
    override suspend fun write(uriString: String, json: String): Unit = withContext(dispatcher) {
        val uri = uriString.toUri()
        val outputStream = context.contentResolver.openOutputStream(uri)
            ?: throw IOException("Could not open output stream for URI: $uriString")
        outputStream.bufferedWriter().use { writer ->
            writer.write(json)
        }
    }
}
