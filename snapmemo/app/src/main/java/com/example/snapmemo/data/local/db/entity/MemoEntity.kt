package com.example.snapmemo.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memos")
data class MemoEntity(
    @PrimaryKey
    val id: String,
    val serverId: String? = null,
    val content: String,
    val state: MemoState,
    val visibility: MemoVisibility,
    val pinned: Boolean = false,
    val creatorName: String? = null,
    val snippet: String? = null,
    val displayTime: Long,
    val createTime: Long,
    val updateTime: Long,
    val hasLink: Boolean = false,
    val hasTaskList: Boolean = false,
    val hasCode: Boolean = false,
    val hasIncompleteTasks: Boolean = false,
    val parentId: String? = null,
    val locationPlaceholder: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val isDeleted: Boolean = false,
    val baseUpdateTime: Long? = null,
    val reminderTime: Long? = null,
    val lastViewedAt: Long? = null
)

enum class MemoState { NORMAL, ARCHIVED }
enum class MemoVisibility { PRIVATE, PROTECTED, PUBLIC }
enum class SyncStatus { SYNCED, PENDING_CREATE, PENDING_UPDATE, PENDING_DELETE, CONFLICT, FAILED }
