package com.example.todowhere.DTO


data class GetAllDto(var result:Result? = null) {
    data class Result(var region:Region? = null) {
        data class Region(
            var area1:Area? = null,
            var area2:Area? = null,
            var area3:Area? = null
        ) {
            data class Area(var name:String? = null)
        }
    }
}