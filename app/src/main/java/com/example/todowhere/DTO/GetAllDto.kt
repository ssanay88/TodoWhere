package com.example.todowhere.DTO


data class GetAllDto(var result:Result) {
    data class Result(var region:Region) {
        data class Region(
            var area1:Area,
            var area2:Area,
            var area3:Area
        ) {
            data class Area(var name:String)
        }
    }
}