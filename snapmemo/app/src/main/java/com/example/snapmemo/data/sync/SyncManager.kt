package com.example.snapmemo.data.sync

import android.util.Log
import com.example.snapmemo.data.local.datastore.AppMode
import com.example.snapmemo.data.local.datastore.UserPreferences
import com.example.snapmemo.data.local.db.dao.MemoDao
import com.example.snapmemo.data.local.db.dao.OutboxDao
import com.example.snapmemo.data.mapper.toEntity
import com.example.snapmemo.data.remote.api.MemosMemoApi
import com.example.snapmemo.util.NetworkMonitor
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val memoDao: MemoDao,
    private val outboxDao: OutboxDao,
    private val memoApi: MemosMemoApi,
    private val outboxProcessor: OutboxProcessor,
    private val networkMonitor: NetworkMonitor,
    private val userPreferences: UserPreferences
) {
    companion object {
        private const val TAG = "SyncManager"
    }

    var isSyncing = false
        private set

    suspend fun sync() {
        val mode = userPreferences.appMode.first()
        if (mode != AppMode.ONLINE) return

        val isOnline = networkMonitor.isOnline.first()
        if (!isOnline) return

        isSyncing = true
        try {
            pullFromServer()
            processOutboxQueue()
            userPreferences.updateLastSyncTime(System.currentTimeMillis())
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
        } finally {
            isSyncing = false
        }
    }

    private suspend fun pullFromServer() {
        var pageToken: String? = null
        do {
            val response = memoApi.listMemos(pageSize = 100, pageToken = pageToken)
            response.memos.forEach { dto ->
                memoDao.insert(dto.toEntity())
            }
            pageToken = response.nextPageToken
        } while (!pageToken.isNullOrEmpty())
    }

    private suspend fun processOutboxQueue() {
        repeat(100) {
            outboxProcessor.processNext()
        }
    }
}
