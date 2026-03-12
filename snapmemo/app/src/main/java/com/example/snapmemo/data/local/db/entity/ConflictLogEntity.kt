package com.example.snapmemo.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conflict_log")
data class ConflictLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memoId: String,
    val localContent: String,
    val remoteContent: String,
    val conflictFields: String,
    val resolution: ConflictResolution,
    val resolvedAt: Long? = null,
    val createdAt: Long
)

enum class ConflictResolution { PENDING, LOCAL, REMOTE, MERGED }
