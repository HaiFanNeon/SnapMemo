package com.example.snapmemo.di

import android.content.Context
import androidx.room.Room
import com.example.snapmemo.data.local.db.AppDatabase
import com.example.snapmemo.data.local.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "memoflow.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideMemoDao(db: AppDatabase): MemoDao = db.memoDao()

    @Provides
    fun provideTagDao(db: AppDatabase): TagDao = db.tagDao()

    @Provides
    fun provideAttachmentDao(db: AppDatabase): AttachmentDao = db.attachmentDao()

    @Provides
    fun provideOutboxDao(db: AppDatabase): OutboxDao = db.outboxDao()

    @Provides
    fun provideAiCacheDao(db: AppDatabase): AiCacheDao = db.aiCacheDao()

    @Provides
    fun provideConflictLogDao(db: AppDatabase): ConflictLogDao = db.conflictLogDao()
}
