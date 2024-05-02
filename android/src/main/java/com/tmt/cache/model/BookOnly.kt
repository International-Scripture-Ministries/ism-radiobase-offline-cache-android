package com.tmt.cache.model

import com.google.gson.annotations.SerializedName

class BookOnly(
  @SerializedName("bibleId")
  val bibleId: String,
  @SerializedName("bookId")
  val bookId: String,
  @SerializedName("abbreviation")
  val abbreviation: String,
  @SerializedName("bible")
  val bible: String,
  @SerializedName("bookOrder")
  val bookOrder: Int,
  @SerializedName("name")
  val name: String,
  @SerializedName("nameLong")
  val nameLong: String,
  @SerializedName("testament")
  val testament: String,
  @SerializedName("number_of_chapters")
  val numberOfChapters: Int,
  @SerializedName("book_name_eng")
  val bookNameEng: String,
  @SerializedName("art_url")
  val art_url: String
)
