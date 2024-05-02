package com.tmt.cache.model

import com.google.gson.annotations.SerializedName

data class MLiveDownload(
    @SerializedName("book_name")
    val bookName: String,
    @SerializedName("file_type")
    val fileType: String,
    @SerializedName("progress")
    val progress: Int,
    @SerializedName("type")
    val type: String
)
