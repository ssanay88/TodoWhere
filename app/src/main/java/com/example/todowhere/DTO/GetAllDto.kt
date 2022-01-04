package com.example.todowhere.DTO



import com.example.todowhere.data.Result
import com.example.todowhere.data.Status

data class GetAllDto(
    val status: Status,
    val results: List<Result>
    )

