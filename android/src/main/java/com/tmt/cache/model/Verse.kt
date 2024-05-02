package com.tmt.cache.model

import com.google.gson.annotations.SerializedName

data class Verse(
        @SerializedName("book_name")
        val bookName: String,
        @SerializedName("book_id")
        val bookId: String,
        @SerializedName("book_order")
        val bookOrder: String,
        @SerializedName("chapter_id")
        val chapterId: String,
        @SerializedName("chapter_title")
        val chapterTitle: String,
        @SerializedName("verse_id")
        val verseId: String,
        @SerializedName("verse_text")
        val verseText: String,
        @SerializedName("paragraph_number")
        val paragraphNumber: String,
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
        val teachingFileName: String,
        @SerializedName("folder_path")
        val folderPath: String,
        @SerializedName("teaching_start_point")
        val teachingStartPoint: String,
        @SerializedName("teaching_end_point")
        val teachingEndPoint: String
)