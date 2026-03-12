package com.example.snapmemo.data.local.db.dao

import androidx.room.*
import com.example.snapmemo.data.local.db.entity.MemoEntity
import com.example.snapmemo.data.local.db.entity.MemoState
import com.example.snapmemo.data.local.db.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

data class DailyStat(
    val day: String,
    val count: Int
)

data class DailyStatEpoch(
    val dayEpoch: Long,
    val count: Int
)

@Dao
interface MemoDao {

    @Query("SELECT * FROM memos WHERE isDeleted = 0 AND state = 'NORMAL' ORDER BY pinned DESC, displayTime DESC")
    fun getActiveNormal(): Flow<List<MemoEntity>>

    @Query("SELECT * FROM memos WHERE isDeleted = 0 AND state = 'ARCHIVED' ORDER BY updateTime DESC")
    fun getArchived(): Flow<List<MemoEntity>>

    @Query("SELECT * FROM memos WHERE isDeleted = 1 ORDER BY updateTime DESC")
    fun getDeleted(): Flow<List<MemoEntity>>

    @Query("SELECT * FROM memos WHERE id = :id")
    suspend fun getById(id: String): MemoEntity?

    @Query("SELECT * FROM memos WHERE isDeleted = 0 AND content LIKE :query ORDER BY displayTime DESC")
    fun search(query: String): Flow<List<MemoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memo: MemoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memos: List<MemoEntity>)

    @Update
    suspend fun update(memo: MemoEntity)

    @Query("UPDATE memos SET isDeleted = 1, syncStatus = :syncStatus, updateTime = :now WHERE id = :id")
    suspend fun softDelete(
        id: String,
        syncStatus: SyncStatus = SyncStatus.PENDING_DELETE,
        now: Long = System.currentTimeMillis()
    )

    @Query("UPDATE memos SET isDeleted = 0, state = 'NORMAL', syncStatus = :syncStatus WHERE id = :id")
    suspend fun restore(id: String, syncStatus: SyncStatus = SyncStatus.PENDING_UPDATE)

    @Query("DELETE FROM memos WHERE id = :id")
    suspend fun hardDelete(id: String)

    @Query("UPDATE memos SET state = 'ARCHIVED', syncStatus = :syncStatus WHERE id = :id")
    suspend fun archive(id: String, syncStatus: SyncStatus = SyncStatus.PENDING_UPDATE)

    @Query("UPDATE memos SET state = 'NORMAL', syncStatus = :syncStatus WHERE id = :id")
    suspend fun unarchive(id: String, syncStatus: SyncStatus = SyncStatus.PENDING_UPDATE)

    @Query("UPDATE memos SET pinned = :pinned, syncStatus = :syncStatus WHERE id = :id")
    suspend fun setPinned(
        id: String,
        pinned: Boolean,
        syncStatus: SyncStatus = SyncStatus.PENDING_UPDATE
    )

    @Query("UPDATE memos SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)

    @Query("UPDATE memos SET serverId = :serverId, syncStatus = 'SYNCED' WHERE id = :id")
    suspend fun updateServerId(id: String, serverId: String)

    @Query("UPDATE memos SET lastViewedAt = :time WHERE id = :id")
    suspend fun updateLastViewedAt(id: String, time: Long)

    @Query("SELECT COUNT(*) FROM memos WHERE syncStatus != 'SYNCED' AND isDeleted = 0")
    fun getPendingSyncCount(): Flow<Int>

    @Query("SELECT * FROM memos WHERE syncStatus IN ('PENDING_CREATE', 'PENDING_UPDATE', 'PENDING_DELETE')")
    suspend fun getPendingSync(): List<MemoEntity>

    @Query("""
        SELECT date(displayTime / 1000, 'unixepoch', 'localtime') as day, COUNT(*) as count
        FROM memos WHERE isDeleted = 0
        GROUP BY day
        ORDER BY day DESC
        LIMIT :days
    """)
    fun getDailyStats(days: Int = 365): Flow<List<DailyStat>>

    @Query("SELECT COUNT(*) FROM memos WHERE isDeleted = 0 AND state = 'NORMAL'")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT date(displayTime / 1000, 'unixepoch', 'localtime')) FROM memos WHERE isDeleted = 0")
    fun getActiveDayCount(): Flow<Int>

    @Query("SELECT * FROM memos WHERE isDeleted = 0 AND state = 'NORMAL' ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomMemo(): MemoEntity?

    @Query("""
        SELECT m.* FROM memos m
        INNER JOIN memo_tags t ON m.id = t.memoId
        WHERE m.id != :excludeId AND m.isDeleted = 0 AND m.state = 'NORMAL'
        AND t.tag IN (SELECT tag FROM memo_tags WHERE memoId = :memoId)
        ORDER BY RANDOM() LIMIT 1
    """)
    suspend fun getRelatedMemo(memoId: String, excludeId: String): MemoEntity?

    @Query("""
        SELECT * FROM memos WHERE isDeleted = 0 AND state = 'NORMAL'
        ORDER BY CASE WHEN lastViewedAt IS NULL THEN 0 ELSE lastViewedAt END ASC
        LIMIT 1
    """)
    suspend fun getLeastRecentlyViewedMemo(): MemoEntity?

    @Query("SELECT * FROM memos WHERE isDeleted = 0 AND state = 'NORMAL' AND id = :id")
    fun getMemoById(id: String): Flow<MemoEntity?>

    @Query("""
        SELECT (strftime('%s', date(createTime / 1000, 'unixepoch', 'localtime')) * 1000) as dayEpoch,
               COUNT(*) as count
        FROM memos WHERE isDeleted = 0 AND createTime >= :since
        GROUP BY dayEpoch
        ORDER BY dayEpoch DESC
    """)
    fun getDailyStats(since: Long): Flow<List<DailyStatEpoch>>

    @Query("SELECT * FROM memos WHERE isDeleted = 0 AND state = 'NORMAL' AND createTime >= :since ORDER BY createTime DESC")
    fun getMemosCreatedAfter(since: Long): Flow<List<MemoEntity>>

    @Query("SELECT * FROM memos WHERE isDeleted = 0 AND state = 'NORMAL' ORDER BY createTime DESC")
    suspend fun getAllActiveSync(): List<MemoEntity>
}
