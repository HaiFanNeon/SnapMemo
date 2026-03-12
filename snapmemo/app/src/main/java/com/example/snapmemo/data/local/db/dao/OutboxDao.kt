package com.example.snapmemo.data.local.db.dao

import androidx.room.*
import com.example.snapmemo.data.local.db.entity.OutboxEntry
import com.example.snapmemo.data.local.db.entity.OutboxStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {

    @Insert
    suspend fun insert(entry: OutboxEntry): Long

    @Query("""
        SELECT * FROM outbox
        WHERE status = 'PENDING' AND nextRetryAt <= :now
        ORDER BY createdAt ASC
        LIMIT 1
    """)
    suspend fun getNextPending(now: Long = System.currentTimeMillis()): OutboxEntry?

    @Query("SELECT * FROM outbox ORDER BY createdAt DESC")
    fun getAll(): Flow<List<OutboxEntry>>

    @Query("SELECT * FROM outbox WHERE status = 'FAILED' ORDER BY createdAt DESC")
    fun getFailed(): Flow<List<OutboxEntry>>

    @Query("SELECT COUNT(*) FROM outbox WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM outbox WHERE status = 'FAILED'")
    fun getFailedCount(): Flow<Int>

    @Query("UPDATE outbox SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: OutboxStatus, completedAt: Long? = null)

    @Query("""
        UPDATE outbox SET
            retryCount = :retryCount,
            nextRetryAt = :nextRetryAt,
            lastError = :lastError,
            status = 'PENDING'
        WHERE id = :id
    """)
    suspend fun updateForRetry(
        id: Long,
        retryCount: Int,
        nextRetryAt: Long,
        lastError: String?
    )

    @Query("UPDATE outbox SET status = 'FAILED' WHERE id = :id")
    suspend fun markFailed(id: Long)

    @Query("UPDATE outbox SET status = 'PENDING', retryCount = 0, nextRetryAt = :now WHERE id = :id")
    suspend fun retryNow(id: Long, now: Long = System.currentTimeMillis())

    @Query("UPDATE outbox SET status = 'CANCELLED' WHERE id = :id")
    suspend fun cancel(id: Long)

    @Query("DELETE FROM outbox WHERE status IN ('COMPLETED', 'CANCELLED') AND completedAt < :before")
    suspend fun cleanupOld(before: Long)
}
