package com.example.snapmemo.domain.repository

import com.example.snapmemo.data.local.db.entity.MemoVisibility
import com.example.snapmemo.domain.model.Memo
import kotlinx.coroutines.flow.Flow

interface MemoRepository {
    fun getMemos(): Flow<List<Memo>>
    fun getArchivedMemos(): Flow<List<Memo>>
    fun getDeletedMemos(): Flow<List<Memo>>
    fun searchMemos(query: String): Flow<List<Memo>>
    fun getMemoById(id: String): Flow<Memo?>
    suspend fun createMemo(content: String, visibility: MemoVisibility, tags: List<String>): Result<Memo>
    suspend fun updateMemo(id: String, content: String, visibility: MemoVisibility, tags: List<String>): Result<Memo>
    suspend fun deleteMemo(id: String): Result<Unit>
    suspend fun archiveMemo(id: String): Result<Unit>
    suspend fun unarchiveMemo(id: String): Result<Unit>
    suspend fun pinMemo(id: String, pinned: Boolean): Result<Unit>
    suspend fun restoreMemo(id: String): Result<Unit>
    suspend fun hardDeleteMemo(id: String): Result<Unit>
    suspend fun getRandomMemo(): Memo?
    suspend fun getRelatedMemo(memoId: String): Memo?
    suspend fun getLeastRecentlyViewedMemo(): Memo?
    suspend fun updateLastViewedAt(id: String, time: Long)
    fun getPendingSyncCount(): Flow<Int>
    fun getTotalCount(): Flow<Int>
    fun getActiveDayCount(): Flow<Int>
}
