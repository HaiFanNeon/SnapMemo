package com.example.snapmemo.data.remote.dto.memo

import com.google.gson.annotations.SerializedName

data class ListMemosResponse(
    @SerializedName("memos")
    val memos: List<MemoDto>,
    @SerializedName("nextPageToken")
    val nextPageToken: String?
)
