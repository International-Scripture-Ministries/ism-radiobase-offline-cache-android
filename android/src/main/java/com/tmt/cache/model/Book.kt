package com.tmt.cache.model

import com.google.gson.annotations.SerializedName
import java.util.*

class Book(
    @SerializedName("dam_id")
    val damId: String,
    @SerializedName("book_id")
    val bookId: String,
    @SerializedName("book_name")
    val bookName: String,
    @SerializedName("book_order")
    val bookOrder: String,
    @SerializedName("number_of_chapters")
    val numberOfChapters: String,
    @SerializedName("chapters")
    val chapters: String,
    @SerializedName("lang_code")
    val langCode: String,
    @SerializedName("chapters_list")
    val chaptersList: ArrayList<Chapters>
)