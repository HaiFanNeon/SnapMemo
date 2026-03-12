package com.example.snapmemo.domain.repository

import android.net.Uri
import com.example.snapmemo.domain.model.Attachment
import kotlinx.coroutines.flow.Flow

interface AttachmentRepository {
    fun getAllAttachments(): Flow<List<Attachment>>
    fun getAttachmentsByMemo(memoId: String): Flow<List<Attachment>>
    suspend fun uploadAttachment(memoId: String?, uri: Uri): Result<Attachment>
    suspend fun deleteAttachment(id: String): Result<Unit>
}
