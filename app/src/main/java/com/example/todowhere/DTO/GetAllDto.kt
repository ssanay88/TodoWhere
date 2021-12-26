package com.example.todowhere.DTO

import com.google.gson.annotations.SerializedName

data class GetAllDto(
    @SerializedName("results") val results:Result
)