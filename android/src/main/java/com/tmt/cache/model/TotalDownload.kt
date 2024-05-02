package com.tmt.cache.model

import com.google.gson.annotations.SerializedName

data class TotalDownload(
    @SerializedName("book_id")
    var book_id: String,
    @SerializedName("book_name")
    var book_name: String
)
