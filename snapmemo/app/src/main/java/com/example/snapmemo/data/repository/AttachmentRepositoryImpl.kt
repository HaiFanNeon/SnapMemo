package com.example.snapmemo.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.snapmemo.data.local.datastore.AppMode
import com.example.snapmemo.data.local.datastore.UserPreferences
import com.example.snapmemo.data.local.db.dao.AttachmentDao
import com.example.snapmemo.data.local.db.dao.OutboxDao
import com.example.snapmemo.data.local.db.entity.*
import com.example.snapmemo.data.mapper.toDomain
import com.example.snapmemo.data.remote.api.MemosAttachmentApi
import com.example.snapmemo.data.remote.dto.attachment.CreateAttachmentRequest
import com.example.snapmemo.domain.model.Attachment
import com.example.snapmemo.domain.repository.AttachmentRepository
import com.example.snapmemo.util.MediaCompressor
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Base64
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttachmentRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val attachmentDao: AttachmentDao,
    private val outboxDao: OutboxDao,
    private val attachmentApi: MemosAttachmentApi,
    private val userPreferences: UserPreferences,
    private val mediaCompressor: MediaCompressor,
    private val gson: Gson
) : AttachmentRepository {

    override fun getAllAttachments(): Flow<List<Attachment>> =
        attachmentDao.getAll().map { it.map { e -> e.toDomain() } }

    override fun getAttachmentsByMemo(memoId: String): Flow<List<Attachment>> =
        attachmentDao.getByMemoId(memoId).map { it.map { e -> e.toDomain() } }

    override suspend fun uploadAttachment(memoId: String?, uri: Uri): Result<Attachment> =
        runCatching {
            val filename = getFileName(uri) ?: "attachment_${System.currentTimeMillis()}"
            val mimeType = mediaCompressor.getMimeType(filename)
            val localId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            // 保存到缓存目录
            val localFile = if (mediaCompressor.isImage(mimeType)) {
                mediaCompressor.compressToFile(uri, "$localId.jpg")
            } else {
                copyToCache(uri, filename)
            }

            val isOnline = userPreferences.appMode.first() == AppMode.ONLINE

            val entity = AttachmentEntity(
                id = localId,
                memoId = memoId,
                filename = filename,
                mimeType = mimeType,
                size = localFile.length(),
                localPath = localFile.absolutePath,
                externalLink = null,
                createTime = now,
                syncStatus = if (isOnline) SyncStatus.PENDING_CREATE else SyncStatus.SYNCED
            )
            attachmentDao.insert(entity)

            if (isOnline) {
                val base64 = withContext(Dispatchers.IO) {
                    Base64.getEncoder().encodeToString(localFile.readBytes())
                }
                val payload = gson.toJson(
                    CreateAttachmentRequest(
                        filename = filename,
                        type = mimeType,
                        content = base64,
                        memo = memoId?.let { "memos/$it" }
                    )
                )
                outboxDao.insert(
                    OutboxEntry(
                        entityType = EntityType.ATTACHMENT,
                        entityId = localId,
                        operation = OutboxOperation.CREATE,
                        payload = payload,
                        createdAt = now,
                        nextRetryAt = now
                    )
                )
            }

            entity.toDomain()
        }

    override suspend fun deleteAttachment(id: String): Result<Unit> = runCatching {
        val entity = attachmentDao.getById(id)
            ?: throw IllegalArgumentException("Attachment not found: $id")
        entity.localPath?.let { File(it).delete() }
        attachmentDao.delete(entity)
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = cursor.getString(idx)
            }
        }
        return name ?: uri.lastPathSegment
    }

    private suspend fun copyToCache(uri: Uri, filename: String): File =
        withContext(Dispatchers.IO) {
            val cacheDir = File(context.cacheDir, "attachments").also { it.mkdirs() }
            val outFile = File(cacheDir, filename)
            context.contentResolver.openInputStream(uri)?.use { input ->
                outFile.outputStream().use { output -> input.copyTo(output) }
            }
            outFile
        }
}
