package com.example.todowhere.data

import com.google.gson.annotations.SerializedName

data class Land(
    @SerializedName("name") val name:String,
    @SerializedName("number1") val number1:String,
    @SerializedName("addition0") val addition0:Addition0
) {
    data class Addition0(
        @SerializedName("value") val value:String
    )
}
// name - number1 - value 순서