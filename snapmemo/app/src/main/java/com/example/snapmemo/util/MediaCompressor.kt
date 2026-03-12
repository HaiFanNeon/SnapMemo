package com.example.snapmemo.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.sqrt

@Singleton
class MediaCompressor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val DEFAULT_MAX_DIMENSION = 1920
        private const val DEFAULT_MAX_SIZE_KB = 500
        private val MIME_MAP = mapOf(
            "jpg" to "image/jpeg",
            "jpeg" to "image/jpeg",
            "png" to "image/png",
            "gif" to "image/gif",
            "webp" to "image/webp",
            "mp4" to "video/mp4",
            "mov" to "video/quicktime",
            "avi" to "video/x-msvideo",
            "mp3" to "audio/mpeg",
            "m4a" to "audio/mp4",
            "ogg" to "audio/ogg",
            "wav" to "audio/wav",
            "pdf" to "application/pdf",
        )
    }

    /**
     * 压缩位图，限制最大边长和文件大小
     */
    suspend fun compressImage(
        bitmap: Bitmap,
        maxSizeKb: Int = DEFAULT_MAX_SIZE_KB,
        maxDimension: Int = DEFAULT_MAX_DIMENSION
    ): CompressResult = withContext(Dispatchers.IO) {
        val scaled = scaleBitmap(bitmap, maxDimension)
        val bytes = compressToBytes(scaled, maxSizeKb)
        CompressResult(
            data = bytes,
            width = scaled.width,
            height = scaled.height,
            size = bytes.size.toLong()
        )
    }

    /**
     * 压缩 Uri 指向的图片文件
     */
    suspend fun compressImageUri(
        uri: Uri,
        maxSizeKb: Int = DEFAULT_MAX_SIZE_KB,
        maxDimension: Int = DEFAULT_MAX_DIMENSION
    ): CompressResult = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open URI: $uri")
        val bitmap = BitmapFactory.decodeStream(inputStream)
            ?: throw IllegalArgumentException("Cannot decode bitmap from URI: $uri")
        inputStream.close()
        compressImage(bitmap, maxSizeKb, maxDimension)
    }

    /**
     * 压缩并保存到缓存目录，返回本地文件路径
     */
    suspend fun compressToFile(
        uri: Uri,
        filename: String,
        maxSizeKb: Int = DEFAULT_MAX_SIZE_KB,
        maxDimension: Int = DEFAULT_MAX_DIMENSION
    ): File = withContext(Dispatchers.IO) {
        val result = compressImageUri(uri, maxSizeKb, maxDimension)
        val cacheDir = File(context.cacheDir, "attachments").also { it.mkdirs() }
        val outFile = File(cacheDir, filename)
        FileOutputStream(outFile).use { it.write(result.data) }
        outFile
    }

    fun getMimeType(filename: String): String {
        val ext = filename.substringAfterLast(".", "").lowercase()
        return MIME_MAP[ext] ?: "application/octet-stream"
    }

    fun isImage(mimeType: String) = mimeType.startsWith("image/")
    fun isVideo(mimeType: String) = mimeType.startsWith("video/")
    fun isAudio(mimeType: String) = mimeType.startsWith("audio/")

    private fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val maxSide = maxOf(bitmap.width, bitmap.height)
        if (maxSide <= maxDimension) return bitmap
        val scale = maxDimension.toFloat() / maxSide
        val newWidth = (bitmap.width * scale).toInt()
        val newHeight = (bitmap.height * scale).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun compressToBytes(bitmap: Bitmap, maxSizeKb: Int): ByteArray {
        val maxBytes = maxSizeKb * 1024
        var quality = 90
        val out = ByteArrayOutputStream()
        do {
            out.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            quality -= 10
        } while (out.size() > maxBytes && quality > 10)
        return out.toByteArray()
    }
}

data class CompressResult(
    val data: ByteArray,
    val width: Int,
    val height: Int,
    val size: Long
)
