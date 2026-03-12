package com.example.snapmemo.domain.model

import com.example.snapmemo.data.local.db.entity.MemoState
import com.example.snapmemo.data.local.db.entity.MemoVisibility
import com.example.snapmemo.data.local.db.entity.SyncStatus
import java.time.Instant

data class Memo(
    val id: String,
    val content: String,
    val state: MemoState,
    val visibility: MemoVisibility,
    val pinned: Boolean,
    val tags: List<String>,
    val attachments: List<Attachment>,
    val displayTime: Instant,
    val createTime: Instant,
    val updateTime: Instant,
    val snippet: String?,
    val property: MemoProperty,
    val syncStatus: SyncStatus,
    val location: Location?
)

data class MemoProperty(
    val hasLink: Boolean,
    val hasTaskList: Boolean,
    val hasCode: Boolean,
    val hasIncompleteTasks: Boolean
)

data class Location(
    val placeholder: String?,
    val latitude: Double?,
    val longitude: Double?
)
