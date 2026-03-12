package com.example.snapmemo.data.local.db.dao

import androidx.room.*
import com.example.snapmemo.data.local.db.entity.AiCacheEntity

@Dao
interface AiCacheDao {

    @Query("SELECT * FROM ai_cache WHERE memoId = :memoId")
    suspend fun getByMemoId(memoId: String): AiCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cache: AiCacheEntity)

    @Query("DELETE FROM ai_cache WHERE memoId = :memoId")
    suspend fun deleteByMemoId(memoId: String)

    @Query("DELETE FROM ai_cache WHERE generatedAt < :before")
    suspend fun cleanupOld(before: Long)
}
