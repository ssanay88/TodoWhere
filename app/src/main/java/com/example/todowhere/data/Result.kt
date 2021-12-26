package com.example.todowhere.data

import com.google.gson.annotations.SerializedName

data class Result(
    @SerializedName("region") val areas:Area
)