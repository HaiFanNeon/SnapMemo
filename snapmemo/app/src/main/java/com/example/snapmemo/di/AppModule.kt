package com.example.snapmemo.di

import com.example.snapmemo.data.remote.api.AiServiceApi
import com.example.snapmemo.data.repository.*
import com.example.snapmemo.domain.repository.*
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindMemoRepository(impl: MemoRepositoryImpl): MemoRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAttachmentRepository(impl: AttachmentRepositoryImpl): AttachmentRepository

    companion object {
        @Provides
        @Singleton
        fun provideAiRepository(
            aiCacheDao: com.example.snapmemo.data.local.db.dao.AiCacheDao,
            gson: Gson,
            aiServiceApi: AiServiceApi?
        ): AiRepository = AiRepositoryImpl(aiCacheDao, gson, aiServiceApi)
    }
}
