package com.example.snapmemo.domain.model

import com.example.snapmemo.data.local.db.entity.EntityType
import com.example.snapmemo.data.local.db.entity.OutboxOperation
import com.example.snapmemo.data.local.db.entity.OutboxStatus
import java.time.Instant

data class SyncState(
    val totalPending: Int,
    val totalFailed: Int,
    val totalCompleted: Int,
    val entries: List<OutboxEntryDetail>,
    val isSyncing: Boolean,
    val lastSyncTime: Instant?
)

data class OutboxEntryDetail(
    val id: Long,
    val entityType: EntityType,
    val operation: OutboxOperation,
    val status: OutboxStatus,
    val retryCount: Int,
    val lastError: String?,
    val createdAt: Instant,
    val nextRetryAt: Instant?
)
