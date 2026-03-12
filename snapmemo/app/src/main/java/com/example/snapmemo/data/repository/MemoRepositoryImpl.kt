package com.example.snapmemo.data.repository

import com.example.snapmemo.data.local.datastore.AppMode
import com.example.snapmemo.data.local.datastore.UserPreferences
import com.example.snapmemo.data.local.db.dao.AttachmentDao
import com.example.snapmemo.data.local.db.dao.MemoDao
import com.example.snapmemo.data.local.db.dao.OutboxDao
import com.example.snapmemo.data.local.db.dao.TagDao
import com.example.snapmemo.data.local.db.entity.*
import com.example.snapmemo.data.mapper.toDomain
import com.example.snapmemo.data.mapper.toEntity
import com.example.snapmemo.data.remote.api.MemosMemoApi
import com.example.snapmemo.data.remote.dto.memo.CreateMemoRequest
import com.example.snapmemo.data.remote.dto.memo.UpdateMemoRequest
import com.example.snapmemo.domain.model.Attachment
import com.example.snapmemo.domain.model.Memo
import com.example.snapmemo.domain.repository.MemoRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoRepositoryImpl @Inject constructor(
    private val memoDao: MemoDao,
    private val tagDao: TagDao,
    private val attachmentDao: AttachmentDao,
    private val outboxDao: OutboxDao,
    private val memoApi: MemosMemoApi,
    private val userPreferences: UserPreferences,
    private val gson: Gson
) : MemoRepository {

    private fun buildMemoFlow(entityFlow: Flow<List<MemoEntity>>): Flow<List<Memo>> =
        entityFlow.map { entities ->
            entities.map { entity ->
                val tags = tagDao.getTagsForMemoSync(entity.id)
                val attachments = attachmentDao.getByMemoIdSync(entity.id)
                    .map { it.toDomain() }
                entity.toDomain(tags, attachments)
            }
        }

    override fun getMemos(): Flow<List<Memo>> = buildMemoFlow(memoDao.getActiveNormal())

    override fun getArchivedMemos(): Flow<List<Memo>> = buildMemoFlow(memoDao.getArchived())

    override fun getDeletedMemos(): Flow<List<Memo>> = buildMemoFlow(memoDao.getDeleted())

    override fun searchMemos(query: String): Flow<List<Memo>> = buildMemoFlow(memoDao.search(query))

    override fun getMemoById(id: String): Flow<Memo?> = memoDao.getMemoById(id).map { entity ->
        entity?.let {
            val tags = tagDao.getTagsForMemoSync(it.id)
            val attachments = attachmentDao.getByMemoIdSync(it.id).map { a -> a.toDomain() }
            it.toDomain(tags, attachments)
        }
    }

    override suspend fun createMemo(
        content: String,
        visibility: MemoVisibility,
        tags: List<String>
    ): Result<Memo> = runCatching {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val isOnline = userPreferences.appMode.first() == AppMode.ONLINE

        val entity = MemoEntity(
            id = id,
            content = content,
            state = MemoState.NORMAL,
            visibility = visibility,
            pinned = false,
            displayTime = now,
            createTime = now,
            updateTime = now,
            syncStatus = if (isOnline) SyncStatus.PENDING_CREATE else SyncStatus.SYNCED
        )
        memoDao.insert(entity)
        tagDao.replaceTagsForMemo(id, tags)

        if (isOnline) {
            val payload = gson.toJson(
                CreateMemoRequest(
                    content = content,
                    state = "NORMAL",
                    visibility = visibility.name
                )
            )
            outboxDao.insert(
                OutboxEntry(
                    entityType = EntityType.MEMO,
                    entityId = id,
                    operation = OutboxOperation.CREATE,
                    payload = payload,
                    createdAt = now,
                    nextRetryAt = now
                )
            )
        }

        entity.toDomain(tags, emptyList())
    }

    override suspend fun updateMemo(
        id: String,
        content: String,
        visibility: MemoVisibility,
        tags: List<String>
    ): Result<Memo> = runCatching {
        val existing = memoDao.getById(id) ?: throw IllegalArgumentException("Memo not found: $id")
        val now = System.currentTimeMillis()
        val isOnline = userPreferences.appMode.first() == AppMode.ONLINE

        val updated = existing.copy(
            content = content,
            visibility = visibility,
            updateTime = now,
            syncStatus = if (isOnline) SyncStatus.PENDING_UPDATE else SyncStatus.SYNCED
        )
        memoDao.update(updated)
        tagDao.replaceTagsForMemo(id, tags)

        if (isOnline) {
            val payload = gson.toJson(UpdateMemoRequest(content = content, state = null, visibility = visibility.name, pinned = null))
            outboxDao.insert(
                OutboxEntry(
                    entityType = EntityType.MEMO,
                    entityId = id,
                    operation = OutboxOperation.UPDATE,
                    payload = payload,
                    createdAt = now,
                    nextRetryAt = now
                )
            )
        }
        updated.toDomain(tags, emptyList())
    }

    override suspend fun deleteMemo(id: String): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        val isOnline = userPreferences.appMode.first() == AppMode.ONLINE
        memoDao.softDelete(id, if (isOnline) SyncStatus.PENDING_DELETE else SyncStatus.SYNCED, now)

        if (isOnline) {
            outboxDao.insert(
                OutboxEntry(
                    entityType = EntityType.MEMO,
                    entityId = id,
                    operation = OutboxOperation.DELETE,
                    payload = "{}",
                    createdAt = now,
                    nextRetryAt = now
                )
            )
        }
    }

    override suspend fun archiveMemo(id: String): Result<Unit> = runCatching {
        memoDao.archive(id)
    }

    override suspend fun unarchiveMemo(id: String): Result<Unit> = runCatching {
        memoDao.unarchive(id)
    }

    override suspend fun pinMemo(id: String, pinned: Boolean): Result<Unit> = runCatching {
        memoDao.setPinned(id, pinned)
    }

    override suspend fun restoreMemo(id: String): Result<Unit> = runCatching {
        memoDao.restore(id)
    }

    override suspend fun hardDeleteMemo(id: String): Result<Unit> = runCatching {
        memoDao.hardDelete(id)
        tagDao.deleteAllForMemo(id)
        attachmentDao.deleteByMemoId(id)
    }

    override suspend fun getRandomMemo(): Memo? =
        memoDao.getRandomMemo()?.let { entity ->
            val tags = tagDao.getTagsForMemoSync(entity.id)
            val attachments = attachmentDao.getByMemoIdSync(entity.id).map { it.toDomain() }
            entity.toDomain(tags, attachments)
        }

    override suspend fun getRelatedMemo(memoId: String): Memo? =
        memoDao.getRelatedMemo(memoId, memoId)?.let { entity ->
            val tags = tagDao.getTagsForMemoSync(entity.id)
            entity.toDomain(tags, emptyList())
        }

    override suspend fun getLeastRecentlyViewedMemo(): Memo? =
        memoDao.getLeastRecentlyViewedMemo()?.let { entity ->
            val tags = tagDao.getTagsForMemoSync(entity.id)
            entity.toDomain(tags, emptyList())
        }

    override suspend fun updateLastViewedAt(id: String, time: Long) {
        memoDao.updateLastViewedAt(id, time)
    }

    override fun getPendingSyncCount(): Flow<Int> = memoDao.getPendingSyncCount()
    override fun getTotalCount(): Flow<Int> = memoDao.getTotalCount()
    override fun getActiveDayCount(): Flow<Int> = memoDao.getActiveDayCount()

    override fun getDailyStats(days: Int): Flow<Map<Long, Int>> {
        val since = System.currentTimeMillis() - days * 86_400_000L
        return memoDao.getDailyStats(since).map { rows ->
            rows.associate { row -> row.dayEpoch to row.count }
        }
    }

    override fun getRecentMemos(days: Int): Flow<List<Memo>> {
        val since = System.currentTimeMillis() - days * 86_400_000L
        return buildMemoFlow(memoDao.getMemosCreatedAfter(since))
    }

    override suspend fun getAllMemosOnce(): List<Memo> =
        memoDao.getAllActiveSync().map { entity ->
            val tags = tagDao.getTagsForMemoSync(entity.id)
            val attachments = attachmentDao.getByMemoIdSync(entity.id).map { it.toDomain() }
            entity.toDomain(tags, attachments)
        }
}
