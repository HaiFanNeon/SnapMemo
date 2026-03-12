package com.example.snapmemo.data.local.db.dao

import androidx.room.*
import com.example.snapmemo.data.local.db.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT DISTINCT tag FROM memo_tags ORDER BY tag")
    fun getAllTags(): Flow<List<String>>

    @Query("SELECT tag FROM memo_tags WHERE memoId = :memoId")
    fun getTagsForMemo(memoId: String): Flow<List<String>>

    @Query("SELECT tag FROM memo_tags WHERE memoId = :memoId")
    suspend fun getTagsForMemoSync(memoId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: TagEntity)

    @Query("DELETE FROM memo_tags WHERE memoId = :memoId")
    suspend fun deleteAllForMemo(memoId: String)

    @Transaction
    suspend fun replaceTagsForMemo(memoId: String, tags: List<String>) {
        deleteAllForMemo(memoId)
        tags.forEach { insert(TagEntity(memoId, it)) }
    }

    @Query("SELECT COUNT(DISTINCT tag) FROM memo_tags")
    fun getTagCount(): Flow<Int>
}
