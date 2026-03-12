package com.example.snapmemo.data.mapper

import com.example.snapmemo.data.local.db.entity.AttachmentEntity
import com.example.snapmemo.data.local.db.entity.SyncStatus
import com.example.snapmemo.data.remote.dto.attachment.AttachmentDto
import com.example.snapmemo.domain.model.Attachment
import java.time.Instant
import java.time.format.DateTimeParseException

fun AttachmentEntity.toDomain(): Attachment = Attachment(
    id = id,
    filename = filename,
    mimeType = mimeType,
    size = size,
    localPath = localPath,
    remoteUrl = externalLink,
    createTime = Instant.ofEpochMilli(createTime)
)

fun AttachmentDto.toEntity(memoId: String? = null): AttachmentEntity {
    val serverId = name.substringAfterLast("/")
    return AttachmentEntity(
        id = serverId,
        serverId = serverId,
        memoId = memoId ?: memo?.substringAfterLast("/"),
        filename = filename,
        mimeType = type,
        size = size?.toLongOrNull() ?: 0L,
        localPath = null,
        externalLink = externalLink,
        createTime = createTime?.parseIsoTime() ?: System.currentTimeMillis(),
        syncStatus = SyncStatus.SYNCED
    )
}

private fun String.parseIsoTime(): Long = try {
    Instant.parse(this).toEpochMilli()
} catch (e: DateTimeParseException) {
    System.currentTimeMillis()
}
