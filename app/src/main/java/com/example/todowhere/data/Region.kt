package com.example.todowhere.data

import com.google.gson.annotations.SerializedName

data class Region(
    @SerializedName("area1")var area1:Area1,
    @SerializedName("area2")var area2:Area2,
    @SerializedName("area3")var area3:Area3
) {
data class Area1(
    @SerializedName("name") val name:String
)

data class Area2(
    @SerializedName("name") val name:String
)

data class Area3(
    @SerializedName("name") val name:String
)



}
