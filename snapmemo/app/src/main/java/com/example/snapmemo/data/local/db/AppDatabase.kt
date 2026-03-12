package com.example.snapmemo.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.snapmemo.data.local.db.dao.*
import com.example.snapmemo.data.local.db.entity.*

@Database(
    entities = [
        MemoEntity::class,
        TagEntity::class,
        AttachmentEntity::class,
        OutboxEntry::class,
        AiCacheEntity::class,
        ConflictLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(AppDatabase.Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun memoDao(): MemoDao
    abstract fun tagDao(): TagDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun outboxDao(): OutboxDao
    abstract fun aiCacheDao(): AiCacheDao
    abstract fun conflictLogDao(): ConflictLogDao

    class Converters {
        @TypeConverter
        fun fromMemoState(value: MemoState): String = value.name

        @TypeConverter
        fun toMemoState(value: String): MemoState = MemoState.valueOf(value)

        @TypeConverter
        fun fromMemoVisibility(value: MemoVisibility): String = value.name

        @TypeConverter
        fun toMemoVisibility(value: String): MemoVisibility = MemoVisibility.valueOf(value)

        @TypeConverter
        fun fromSyncStatus(value: SyncStatus): String = value.name

        @TypeConverter
        fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)

        @TypeConverter
        fun fromEntityType(value: EntityType): String = value.name

        @TypeConverter
        fun toEntityType(value: String): EntityType = EntityType.valueOf(value)

        @TypeConverter
        fun fromOutboxOperation(value: OutboxOperation): String = value.name

        @TypeConverter
        fun toOutboxOperation(value: String): OutboxOperation = OutboxOperation.valueOf(value)

        @TypeConverter
        fun fromOutboxStatus(value: OutboxStatus): String = value.name

        @TypeConverter
        fun toOutboxStatus(value: String): OutboxStatus = OutboxStatus.valueOf(value)

        @TypeConverter
        fun fromConflictResolution(value: ConflictResolution): String = value.name

        @TypeConverter
        fun toConflictResolution(value: String): ConflictResolution = ConflictResolution.valueOf(value)
    }
}
