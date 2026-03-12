package com.example.snapmemo.data.sync

import android.util.Log
import com.example.snapmemo.data.local.db.dao.MemoDao
import com.example.snapmemo.data.local.db.dao.OutboxDao
import com.example.snapmemo.data.local.db.entity.*
import com.example.snapmemo.data.remote.api.MemosAttachmentApi
import com.example.snapmemo.data.remote.api.MemosMemoApi
import com.example.snapmemo.data.remote.dto.memo.CreateMemoRequest
import com.example.snapmemo.data.remote.dto.memo.UpdateMemoRequest
import com.example.snapmemo.util.NetworkMonitor
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import java.io.IOException
import java.time.Duration
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.pow

@Singleton
class OutboxProcessor @Inject constructor(
    private val outboxDao: OutboxDao,
    private val memoDao: MemoDao,
    private val memoApi: MemosMemoApi,
    private val attachmentApi: MemosAttachmentApi,
    private val networkMonitor: NetworkMonitor,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "OutboxProcessor"
    }

    suspend fun processNext() {
        val isOnline = networkMonitor.isOnline.first()
        if (!isOnline) return

        val entry = outboxDao.getNextPending() ?: return

        outboxDao.updateStatus(entry.id, OutboxStatus.PROCESSING)

        try {
            when (entry.entityType) {
                EntityType.MEMO -> processMemoEntry(entry)
                EntityType.ATTACHMENT -> processAttachmentEntry(entry)
                EntityType.TAG -> {}
            }
        } catch (e: IOException) {
            handleRetry(entry, e.message)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error processing outbox entry ${entry.id}", e)
            handleRetry(entry, e.message)
        }
    }

    private suspend fun processMemoEntry(entry: OutboxEntry) {
        when (entry.operation) {
            OutboxOperation.CREATE -> {
                val request = gson.fromJson(entry.payload, CreateMemoRequest::class.java)
                val response = memoApi.createMemo(request)
                val serverId = response.name.substringAfterLast("/")
                memoDao.updateServerId(entry.entityId, serverId)
                outboxDao.updateStatus(entry.id, OutboxStatus.COMPLETED, System.currentTimeMillis())
            }
            OutboxOperation.UPDATE -> {
                val memo = memoDao.getById(entry.entityId) ?: return
                val serverId = memo.serverId ?: return
                val request = gson.fromJson(entry.payload, UpdateMemoRequest::class.java)
                memoApi.updateMemo(serverId, "content,visibility,pinned", request)
                memoDao.updateSyncStatus(entry.entityId, SyncStatus.SYNCED)
                outboxDao.updateStatus(entry.id, OutboxStatus.COMPLETED, System.currentTimeMillis())
            }
            OutboxOperation.DELETE -> {
                val memo = memoDao.getById(entry.entityId) ?: run {
                    outboxDao.updateStatus(entry.id, OutboxStatus.COMPLETED, System.currentTimeMillis())
                    return
                }
                val serverId = memo.serverId
                if (serverId != null) {
                    memoApi.deleteMemo(serverId)
                }
                outboxDao.updateStatus(entry.id, OutboxStatus.COMPLETED, System.currentTimeMillis())
            }
        }
    }

    private suspend fun processAttachmentEntry(entry: OutboxEntry) {
        outboxDao.updateStatus(entry.id, OutboxStatus.COMPLETED, System.currentTimeMillis())
    }

    private suspend fun handleRetry(entry: OutboxEntry, errorMessage: String?) {
        val newRetryCount = entry.retryCount + 1
        if (newRetryCount >= entry.maxRetries) {
            outboxDao.updateStatus(entry.id, OutboxStatus.FAILED)
        } else {
            val delay = calculateNextRetryDelay(newRetryCount)
            outboxDao.updateForRetry(
                id = entry.id,
                retryCount = newRetryCount,
                nextRetryAt = System.currentTimeMillis() + delay,
                lastError = errorMessage
            )
        }
    }

    private fun calculateNextRetryDelay(retryCount: Int): Long {
        val baseDelay = 5_000L
        val maxDelay = 30 * 60 * 1000L
        val jitter = Random().nextLong(0, 1000)
        val delay = (baseDelay * (2.0.pow(retryCount))).toLong()
        return min(delay, maxDelay) + jitter
    }
}
