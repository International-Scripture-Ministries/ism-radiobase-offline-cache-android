package com.tmt.cache.model

import com.google.gson.annotations.SerializedName

data class DownloadData(
    @SerializedName("download_id")
    var downloadId: Long,
    @SerializedName("book_id")
    var bookId: String,
    @SerializedName("file_type")
    var fileType: String,
    @SerializedName("download_url")
    var downloadUrl: String,
    @SerializedName("file_path")
    var filePath: String,
    @SerializedName("file_name")
    var fileName: String,
    @SerializedName("chapter")
    var chapter: String,
    @SerializedName("uuid")
    var uuid: String,
    @SerializedName("status")
    var status: String
)
