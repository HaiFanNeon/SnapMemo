package com.example.snapmemo.data.mapper

import com.example.snapmemo.data.local.db.entity.*
import com.example.snapmemo.data.remote.dto.memo.MemoDto
import com.example.snapmemo.domain.model.*
import java.time.Instant
import java.time.format.DateTimeParseException

fun MemoEntity.toDomain(
    tags: List<String> = emptyList(),
    attachments: List<Attachment> = emptyList()
): Memo = Memo(
    id = id,
    content = content,
    state = state,
    visibility = visibility,
    pinned = pinned,
    tags = tags,
    attachments = attachments,
    displayTime = Instant.ofEpochMilli(displayTime),
    createTime = Instant.ofEpochMilli(createTime),
    updateTime = Instant.ofEpochMilli(updateTime),
    snippet = snippet,
    property = MemoProperty(hasLink, hasTaskList, hasCode, hasIncompleteTasks),
    syncStatus = syncStatus,
    location = if (locationPlaceholder != null || latitude != null || longitude != null) {
        Location(locationPlaceholder, latitude, longitude)
    } else null
)

fun MemoDto.toEntity(syncStatus: SyncStatus = SyncStatus.SYNCED): MemoEntity {
    val serverId = name.substringAfterLast("/")
    return MemoEntity(
        id = serverId,
        serverId = serverId,
        content = content,
        state = try { MemoState.valueOf(state) } catch (e: Exception) { MemoState.NORMAL },
        visibility = try { MemoVisibility.valueOf(visibility) } catch (e: Exception) { MemoVisibility.PRIVATE },
        pinned = pinned,
        snippet = snippet,
        displayTime = displayTime?.parseIsoTime() ?: System.currentTimeMillis(),
        createTime = createTime?.parseIsoTime() ?: System.currentTimeMillis(),
        updateTime = updateTime?.parseIsoTime() ?: System.currentTimeMillis(),
        hasLink = property?.hasLink ?: false,
        hasTaskList = property?.hasTaskList ?: false,
        hasCode = property?.hasCode ?: false,
        hasIncompleteTasks = property?.hasIncompleteTasks ?: false,
        locationPlaceholder = this.location?.placeholder,
        latitude = this.location?.latitude,
        longitude = this.location?.longitude,
        syncStatus = syncStatus
    )
}

private fun String.parseIsoTime(): Long = try {
    Instant.parse(this).toEpochMilli()
} catch (e: DateTimeParseException) {
    System.currentTimeMillis()
}
