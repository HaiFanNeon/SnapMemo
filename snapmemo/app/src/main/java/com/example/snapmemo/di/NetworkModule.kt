package com.example.snapmemo.di

import com.example.snapmemo.data.local.datastore.UserPreferences
import com.example.snapmemo.data.remote.api.AiServiceApi
import com.example.snapmemo.data.remote.api.MemosAttachmentApi
import com.example.snapmemo.data.remote.api.MemosAuthApi
import com.example.snapmemo.data.remote.api.MemosMemoApi
import com.example.snapmemo.data.remote.interceptor.AuthInterceptor
import com.example.snapmemo.data.remote.interceptor.RetryInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        retryInterceptor: RetryInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(retryInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, prefs: UserPreferences, gson: Gson): Retrofit {
        val baseUrl = runBlocking { prefs.serverUrl.first() } ?: "http://localhost/"
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): MemosAuthApi =
        retrofit.create(MemosAuthApi::class.java)

    @Provides
    @Singleton
    fun provideMemoApi(retrofit: Retrofit): MemosMemoApi =
        retrofit.create(MemosMemoApi::class.java)

    @Provides
    @Singleton
    fun provideAttachmentApi(retrofit: Retrofit): MemosAttachmentApi =
        retrofit.create(MemosAttachmentApi::class.java)

    /**
     * AI 服务 API：默认使用 OpenAI 兼容格式，baseUrl 可在设置中配置
     * 当前未配置时返回 null，AiRepositoryImpl 会降级为本地占位摘要
     */
    @Provides
    @Singleton
    fun provideAiServiceApi(prefs: UserPreferences, gson: Gson): AiServiceApi? {
        val aiBaseUrl = runBlocking { prefs.aiBaseUrl.first() } ?: return null
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = runBlocking { prefs.aiApiKey.first() } ?: ""
                val req = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(req)
            }
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(aiBaseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AiServiceApi::class.java)
    }
}
