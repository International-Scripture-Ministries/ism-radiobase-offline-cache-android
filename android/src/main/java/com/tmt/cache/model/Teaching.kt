package com.tmt.cache.model

import com.google.gson.annotations.SerializedName

data class Teaching(
        @SerializedName("book_name")
        val bookName: String,
        @SerializedName("book_id")
        val bookId: String,
        @SerializedName("teaching_id")
        val teachingId: String,
        @SerializedName("teaching_audio")
        val teachingAudio: String,
        @SerializedName("teaching_name")
        val teachingName: String,
        @SerializedName("teachings_badge_image")
        val teachingsBadgeImage: String,
        @SerializedName("bible_book")
        val bibleBook: String,
        @SerializedName("bible_book_eng")
        val bibleBookEng: String,
        @SerializedName("teaching_file_name")
        val teachingFileName: String
)