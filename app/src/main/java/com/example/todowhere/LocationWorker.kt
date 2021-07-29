package com.example.todowhere

import android.Manifest
import android.content.ContentProviderClient
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource

private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

class LocationWorker(context: Context , workerparams:WorkerParameters) : Worker(context , workerparams)  {

    private val wContext = context

    // 권한 확인에 대한 변수
    private var Fine_Permission_Check = ContextCompat.checkSelfPermission(wContext, Manifest.permission.ACCESS_FINE_LOCATION)
    private var Background_Permission_Check = ContextCompat.checkSelfPermission(wContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION)


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var now_location : Location ? = null

    // WorkManager에서 제공하는 백그라운드 스레드에서 비동기적으로 실행
    // doWork에서 반환되는 Result는 작업이 성공적인지 아닌지 판단 , 실패일 경우 WorkManager에 작업을 재실행해야하는지 알려줌
    override fun doWork(): Result {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(wContext)
        // 권한을 우선 확인해야함
        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->

        }
        return Result.success()
    }

    override fun UpdateLocation() {
        if (Fine_Permission_Check == PackageManager.PERMISSION_DENIED || Background_Permission_Check == PackageManager.PERMISSION_DENIED){
            // 둘 중 하나라도 권한이 거부된 경우

        }
    }


}