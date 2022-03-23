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
import androidx.work.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.coroutineScope

private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

// 매일 리셋해야하는 행동
// 1. 완료하지 못한 할 일 들 모두 'Finish' 처리
// 2. Todo리스트와 지오펜싱 리스트 클리어 이후 다음날짜의 Todo와 지오펜싱 추가
// 3.
//  https://zladnrms.tistory.com/157   Coroutineworker로 다시 만들어야 함
@SuppressLint("MissingPermission")
class ResetWorker(context: Context , workerparams:WorkerParameters) : CoroutineWorker(context , workerparams)  {

    var TAG: String = "로그"

    private val wContext = context

    // WorkManager에서 제공하는 백그라운드 스레드에서 비동기적으로 실행
    // doWork에서 반환되는 Result는 작업이 성공적인지 아닌지 판단 , 실패일 경우 WorkManager에 작업을 재실행해야하는지 알려줌
    override suspend fun doWork(): Result = coroutineScope {

        val dailyWorkRequest = OneTimeWorkRequestBuilder<ResetWorker>()
            .setInitialDelay(getTimeUsingInWorkRequest(), java.util.concurrent.TimeUnit.MILLISECONDS)
            .addTag("TAG_OUTPUT")
            .build()

        // 매일 반복하기 위해 리퀘스트를 다시 요청
       WorkManager.getInstance(applicationContext).enqueue(dailyWorkRequest)

        // Todo 매일 반복해야할 행동

       Result.success()
    }


    // 실행 지연 시간을 설정하는 함수
    // OneTimeWorkRequest를 등록할 때마다, 아래 함수의 리턴값만큼 대기한 이후부터 시작하도록 하기 위해 사용
    fun getTimeUsingInWorkRequest() : Long {
        val currentDate = java.util.Calendar.getInstance()
        val dueDate = java.util.Calendar.getInstance()
        // Set Execution around 03:00:00 AM
        dueDate.set(java.util.Calendar.HOUR_OF_DAY, 3)
        dueDate.set(java.util.Calendar.MINUTE, 0)
        dueDate.set(java.util.Calendar.SECOND, 0)

        if(dueDate.before(currentDate)) {
            dueDate.add(java.util.Calendar.HOUR_OF_DAY, 24)
        }

        return dueDate.timeInMillis - currentDate.timeInMillis
    }



}