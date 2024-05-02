package com.tmt.cache.model

import com.google.gson.annotations.SerializedName

data class BookDownload(
    @SerializedName("chapter")
    val chapter: ArrayList<Chapter>,
    @SerializedName("teaching")
    val teaching: ArrayList<Teaching>
) {
    data class Chapter(
        @SerializedName("chapterNumber")
        val chapterNumber: String,
        @SerializedName("audio_path")
        val audioPath: String
    )
    data class Teaching(
        @SerializedName("uuid")
        val uuid: String,
        @SerializedName("name")
        val name: String,
        @SerializedName("description")
        val description: String,
        @SerializedName("audio_duration")
        val audioDuration: String,
        @SerializedName("mime_type")
        val mimeType: String,
        @SerializedName("scheduled_date")
        val scheduledDate: String,
        @SerializedName("audio_path")
        val audioPath: String
    )
}