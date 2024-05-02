package com.tmt.cache.model

import com.google.gson.annotations.SerializedName

data class MVerse(
  @SerializedName("bibleId")
  val bibleId: String,
  @SerializedName("verseId")
  val verseId: String,
  @SerializedName("bookId")
  val bookId: String,
  @SerializedName("chapterId")
  val chapterId: String,
  @SerializedName("content")
  val content: String,
  @SerializedName("reference")
  val reference: String,
  @SerializedName("verseNumber")
  val verseNumber: String,
  @SerializedName("chapterNumber")
  val chapterNumber: Int,
  @SerializedName("teachings")
  val teachings: Teachings
) {
  data class Teachings(
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("audio_duration")
    val audioDuration: Int,
    @SerializedName("audio_format")
    val audioFormat: String,
    @SerializedName("bible_book")
    val bibleBook: String,
    @SerializedName("bible_chapter_end")
    val bibleChapterEnd: Int,
    @SerializedName("bible_chapter_start")
    val bibleChapterStart: Int,
    @SerializedName("bible_verse_end")
    val bibleVerseEnd: Int,
    @SerializedName("bible_verse_start")
    val bibleVerseStart: Int,
    @SerializedName("created")
    val created: String,
    @SerializedName("created_by")
    val createdBy: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("language")
    val language: String,
    @SerializedName("mime_type")
    val mimeType: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("scheduled_date")
    val scheduledDate: String
  )
}
