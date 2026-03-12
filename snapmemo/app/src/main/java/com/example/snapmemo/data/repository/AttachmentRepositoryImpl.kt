package com.example.snapmemo.data.repository

import android.net.Uri
import com.example.snapmemo.data.local.db.dao.AttachmentDao
import com.example.snapmemo.data.mapper.toDomain
import com.example.snapmemo.domain.model.Attachment
import com.example.snapmemo.domain.repository.AttachmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttachmentRepositoryImpl @Inject constructor(
    private val attachmentDao: AttachmentDao
) : AttachmentRepository {

    override fun getAllAttachments(): Flow<List<Attachment>> =
        attachmentDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun getAttachmentsByMemo(memoId: String): Flow<List<Attachment>> =
        attachmentDao.getByMemoId(memoId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun uploadAttachment(memoId: String?, uri: Uri): Result<Attachment> {
        return Result.failure(UnsupportedOperationException("Upload not yet implemented"))
    }

    override suspend fun deleteAttachment(id: String): Result<Unit> = runCatching {
        val entity = attachmentDao.getById(id) ?: return Result.failure(IllegalArgumentException("Not found"))
        attachmentDao.delete(entity)
    }
}
