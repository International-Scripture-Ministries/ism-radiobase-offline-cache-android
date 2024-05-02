package com.tmt.cache.model

import com.google.gson.annotations.SerializedName

data class MNVerse(
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
  val chapterNumber: Int
)
