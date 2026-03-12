package com.example.snapmemo.ui.sync

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapmemo.data.local.db.dao.OutboxDao
import com.example.snapmemo.data.local.db.entity.OutboxEntry
import com.example.snapmemo.data.local.db.entity.OutboxStatus
import com.example.snapmemo.data.local.datastore.UserPreferences
import com.example.snapmemo.data.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class SyncUiState(
    val entries: List<OutboxEntry> = emptyList(),
    val pendingCount: Int = 0,
    val failedCount: Int = 0,
    val isSyncing: Boolean = false,
    val lastSyncTime: Instant? = null
)
@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class SyncStatusViewModel @Inject constructor(
    private val outboxDao: OutboxDao,
    private val userPreferences: UserPreferences,
    private val syncManager: SyncManager
) : ViewModel() {

    val uiState: StateFlow<SyncUiState> = combine(
        outboxDao.getAll(),
        outboxDao.getPendingCount(),
        outboxDao.getFailedCount(),
        userPreferences.lastSyncTime
    ) { entries, pending, failed, lastSync ->
        SyncUiState(
            entries = entries,
            pendingCount = pending,
            failedCount = failed,
            isSyncing = syncManager.isSyncing,
            lastSyncTime = lastSync?.let { Instant.ofEpochMilli(it) }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncUiState())

    fun retryEntry(id: Long) {
        viewModelScope.launch { outboxDao.retryNow(id) }
    }

    fun cancelEntry(id: Long) {
        viewModelScope.launch { outboxDao.cancel(id) }
    }

    fun triggerSync() {
        viewModelScope.launch { syncManager.sync() }
    }
}
