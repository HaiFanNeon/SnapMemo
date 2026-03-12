package com.example.snapmemo.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outbox")
data class OutboxEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entityType: EntityType,
    val entityId: String,
    val operation: OutboxOperation,
    val payload: String,
    val retryCount: Int = 0,
    val maxRetries: Int = 5,
    val lastError: String? = null,
    val status: OutboxStatus = OutboxStatus.PENDING,
    val createdAt: Long,
    val nextRetryAt: Long,
    val completedAt: Long? = null
)

enum class EntityType { MEMO, ATTACHMENT, TAG }
enum class OutboxOperation { CREATE, UPDATE, DELETE }
enum class OutboxStatus { PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED }
