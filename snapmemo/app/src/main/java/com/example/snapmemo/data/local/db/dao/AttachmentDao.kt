package com.example.snapmemo.data.local.db.dao

import androidx.room.*
import com.example.snapmemo.data.local.db.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {

    @Query("SELECT * FROM attachments WHERE memoId = :memoId")
    fun getByMemoId(memoId: String): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE memoId = :memoId")
    suspend fun getByMemoIdSync(memoId: String): List<AttachmentEntity>

    @Query("SELECT * FROM attachments ORDER BY createTime DESC")
    fun getAll(): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE mimeType LIKE :typePrefix || '%' ORDER BY createTime DESC")
    fun getByType(typePrefix: String): Flow<List<AttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: AttachmentEntity)

    @Delete
    suspend fun delete(attachment: AttachmentEntity)

    @Query("SELECT * FROM attachments WHERE id = :id")
    suspend fun getById(id: String): AttachmentEntity?

    @Query("DELETE FROM attachments WHERE memoId = :memoId")
    suspend fun deleteByMemoId(memoId: String)
}
