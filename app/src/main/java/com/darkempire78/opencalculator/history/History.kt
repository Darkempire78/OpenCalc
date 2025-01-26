package com.darkempire78.opencalculator.history

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class History(
    @SerializedName("calculation") var calculation: String,
    @SerializedName("result") var result: String,
    @SerializedName("time") var time: String,
    @SerializedName("id") var id: String = UUID.randomUUID().toString()
)
