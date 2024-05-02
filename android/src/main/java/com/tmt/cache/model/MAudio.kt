package com.tmt.cache.model

import com.google.gson.annotations.SerializedName

data class MAudio(
  @SerializedName("bibleId")
  val bibleId: String,
  @SerializedName("bookId")
  val bookId: String,
  @SerializedName("copyright")
  val copyright: String,
  @SerializedName("createdAt")
  val createdAt: String,
  @SerializedName("id")
  val id: String,
  @SerializedName("next")
  val next: Next,
  @SerializedName("number")
  val number: String,
  @SerializedName("previous")
  val previous: Previous,
  @SerializedName("reference")
  val reference: String,
  @SerializedName("updatedAt")
  val updatedAt: String,
  @SerializedName("url")
  val url: String
) {

  data class Next(
    @SerializedName("id")
    val id: String,
    @SerializedName("number")
    val number: String
  )


  data class Previous(
    @SerializedName("id")
    val id: String,
    @SerializedName("number")
    val number: String
  )
}
