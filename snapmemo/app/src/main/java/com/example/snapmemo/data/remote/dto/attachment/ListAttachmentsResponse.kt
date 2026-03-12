package com.example.snapmemo.data.remote.dto.attachment

import com.google.gson.annotations.SerializedName

data class ListAttachmentsResponse(
    @SerializedName("attachments")
    val attachments: List<AttachmentDto>,
    @SerializedName("nextPageToken")
    val nextPageToken: String?,
    @SerializedName("totalSize")
    val totalSize: Int?
)
