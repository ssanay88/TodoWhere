package com.example.todowhere

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentProviderClient
import android.content.Context
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.icu.util.TimeUnit
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource

private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

//  https://zladnrms.tistory.com/157   Coroutineworker로 다시 만들어야 함
@SuppressLint("MissingPermission")
class ResetWorker(context: Context , workerparams:WorkerParameters) : Worker(context , workerparams)  {

    var TAG: String = "로그"

    private val wContext = context

    // WorkManager에서 제공하는 백그라운드 스레드에서 비동기적으로 실행
    // doWork에서 반환되는 Result는 작업이 성공적인지 아닌지 판단 , 실패일 경우 WorkManager에 작업을 재실행해야하는지 알려줌
    override fun doWork(): Result {

        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        // Set Execution around 03:00:00 AM
        dueDate.set(Calendar.HOUR_OF_DAY, 3)
        dueDate.set(Calendar.MINUTE, 0)
        dueDate.set(Calendar.SECOND, 0)
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        val dailyWorkRequest = OneTimeWorkRequestBuilder<ResetWorker>()
            .setInitialDelay(timeDiff, java.util.concurrent.TimeUnit.MILLISECONDS)
            .addTag("TAG_OUTPUT")
            .build()

        // 매일 반복하기 위해 리퀘스트를 다시 요청
       WorkManager.getInstance(applicationContext)
            .enqueue(dailyWorkRequest)

        return Result.success()
    }




}