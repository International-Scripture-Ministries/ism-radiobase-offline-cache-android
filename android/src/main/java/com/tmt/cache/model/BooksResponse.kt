package com.tmt.cache.model

import  com.google.gson.annotations.SerializedName
import java.util.*

class BooksResponse(
        @SerializedName("old_testament")
        val oldTestament: ArrayList<BookOnly>,
        @SerializedName("new_testament")
        val newTestament: ArrayList<BookOnly>,
        @SerializedName("guidelines")
        val guidelines: ArrayList<BookOnly>
)