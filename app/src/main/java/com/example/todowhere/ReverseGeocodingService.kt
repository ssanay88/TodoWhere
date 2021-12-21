package com.example.todowhere

import com.naver.maps.geometry.LatLng
import retrofit2.http.GET
import retrofit2.http.Query

interface ReverseGeocodingService {

    // https://naveropenapi.apigw.ntruss.com : 기본 URL
    @GET("/map-reversegeocode/v2/gc?output=json&request=coordsToaddr")
    fun getGeocoding(
        @Query("coords") coords:String
    )

}