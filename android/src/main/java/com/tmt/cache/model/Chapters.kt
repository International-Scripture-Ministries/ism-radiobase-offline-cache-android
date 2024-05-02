package com.tmt.cache.model

import com.google.gson.annotations.SerializedName
import com.tmt.cache.model.Verse
import java.util.*

data class Chapters(
        @SerializedName("chapter_id")
        val chapterId: String,
        @SerializedName("copyright_info")
        val copyrightInfo: String,
        @SerializedName("chapter_audio")
        val chapterAudio: String,
        @SerializedName("verses")
        val verses: ArrayList<Verse>
)