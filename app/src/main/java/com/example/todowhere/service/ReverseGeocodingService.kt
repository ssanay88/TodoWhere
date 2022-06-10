package com.example.todowhere.service

import com.example.todowhere.DTO.GetAllDto
import com.naver.maps.geometry.LatLng
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ReverseGeocodingService {

    // https://naveropenapi.apigw.ntruss.com : 기본 URL
    @GET("/map-reversegeocode/v2/gc?output=json&request=coordsToaddr&orders=roadaddr")
    fun getGeocoding(
        @Query("X-NCP-APIGW-API-KEY-ID") apiKeyID:String,
        @Query("X-NCP-APIGW-API-KEY") apiKey:String,
        @Query("coords") coords:String
    ): Call<GetAllDto>

}