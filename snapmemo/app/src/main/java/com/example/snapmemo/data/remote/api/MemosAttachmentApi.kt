package com.example.snapmemo.data.remote.api

import com.example.snapmemo.data.remote.dto.attachment.*
import retrofit2.http.*

interface MemosAttachmentApi {

    @GET("api/v1/attachments")
    suspend fun listAttachments(
        @Query("pageSize") pageSize: Int = 50,
        @Query("pageToken") pageToken: String? = null,
        @Query("filter") filter: String? = null,
        @Query("orderBy") orderBy: String? = null
    ): ListAttachmentsResponse

    @POST("api/v1/attachments")
    suspend fun createAttachment(@Body request: CreateAttachmentRequest): AttachmentDto

    @GET("api/v1/attachments/{attachment}")
    suspend fun getAttachment(@Path("attachment") attachmentId: String): AttachmentDto

    @DELETE("api/v1/attachments/{attachment}")
    suspend fun deleteAttachment(@Path("attachment") attachmentId: String)
}
