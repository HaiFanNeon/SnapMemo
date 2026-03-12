package com.example.snapmemo.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attachments")
data class AttachmentEntity(
    @PrimaryKey
    val id: String,
    val serverId: String? = null,
    val memoId: String?,
    val filename: String,
    val mimeType: String,
    val size: Long,
    val localPath: String?,
    val externalLink: String?,
    val createTime: Long,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
