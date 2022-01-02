package com.example.todowhere.data

import com.google.gson.annotations.SerializedName

data class Result(
        @SerializedName("name") val name:String,
    @SerializedName("region") val region: Region
)
