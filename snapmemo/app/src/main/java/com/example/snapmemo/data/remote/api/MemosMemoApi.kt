package com.example.snapmemo.data.remote.api

import com.example.snapmemo.data.remote.dto.memo.*
import retrofit2.http.*

interface MemosMemoApi {

    @GET("api/v1/memos")
    suspend fun listMemos(
        @Query("pageSize") pageSize: Int = 50,
        @Query("pageToken") pageToken: String? = null,
        @Query("state") state: String? = null,
        @Query("orderBy") orderBy: String? = "display_time desc",
        @Query("filter") filter: String? = null,
        @Query("showDeleted") showDeleted: Boolean = false
    ): ListMemosResponse

    @GET("api/v1/memos/{memo}")
    suspend fun getMemo(@Path("memo") memoId: String): MemoDto

    @POST("api/v1/memos")
    suspend fun createMemo(@Body request: CreateMemoRequest): MemoDto

    @PATCH("api/v1/memos/{memo}")
    suspend fun updateMemo(
        @Path("memo") memoId: String,
        @Query("updateMask") updateMask: String,
        @Body request: UpdateMemoRequest
    ): MemoDto

    @DELETE("api/v1/memos/{memo}")
    suspend fun deleteMemo(@Path("memo") memoId: String)
}
