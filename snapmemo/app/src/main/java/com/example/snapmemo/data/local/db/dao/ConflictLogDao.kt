package com.example.snapmemo.data.local.db.dao

import androidx.room.*
import com.example.snapmemo.data.local.db.entity.ConflictLogEntity
import com.example.snapmemo.data.local.db.entity.ConflictResolution
import kotlinx.coroutines.flow.Flow

@Dao
interface ConflictLogDao {

    @Insert
    suspend fun insert(log: ConflictLogEntity): Long

    @Query("SELECT * FROM conflict_log WHERE resolution = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingConflicts(): Flow<List<ConflictLogEntity>>

    @Query("SELECT * FROM conflict_log ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ConflictLogEntity>>

    @Query("UPDATE conflict_log SET resolution = :resolution, resolvedAt = :resolvedAt WHERE id = :id")
    suspend fun resolve(id: Long, resolution: ConflictResolution, resolvedAt: Long)

    @Query("DELETE FROM conflict_log WHERE createdAt < :before")
    suspend fun cleanupOld(before: Long)
}
