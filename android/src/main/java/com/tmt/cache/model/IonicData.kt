package com.tmt.cache.model

import com.google.gson.annotations.SerializedName

data class IonicData(
    @SerializedName("book_id")
    var bookId: String,
    @SerializedName("file_type")
    var fileType: String,
    @SerializedName("chapterNumber")
    var chapterNumber: String,
    @SerializedName("uuid")
    var uuid: String
)
