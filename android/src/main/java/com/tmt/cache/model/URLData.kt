package com.tmt.cache.model

import com.google.gson.annotations.SerializedName

data class URLData(
    @SerializedName("remoteUrl")
    var remoteUrl: String,
    @SerializedName("id")
    var id: String
)
