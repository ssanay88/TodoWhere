package com.example.todowhere.data

import com.google.gson.annotations.SerializedName

// 각 지역 명칭을 담을 Data class
data class Area(
    val area1:AreaName,
    val area2:AreaName,
    val area3:AreaName
)