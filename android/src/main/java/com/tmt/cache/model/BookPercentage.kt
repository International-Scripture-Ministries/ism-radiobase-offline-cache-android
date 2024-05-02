package com.tmt.cache.model

import com.google.gson.annotations.SerializedName

data class BookPercentage(
    @SerializedName("book_id")
    var book_id: String,
    @SerializedName("download_status")
    var download_status: String,
    @SerializedName("download_percentage")
    var download_percentage: Int
)
