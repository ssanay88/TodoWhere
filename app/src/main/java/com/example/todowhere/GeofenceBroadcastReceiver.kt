package com.example.todowhere

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import io.realm.Realm
import java.util.*

//
class GeofenceBroadcastReceiver(var appState:String,var todayTodo: MutableList<Todo>) : BroadcastReceiver() {

    var TAG: String = "로그"

    val calendar: Calendar = Calendar.getInstance()    // 오늘 날짜로 캘린더 객체 생성
    var today_date = getDate(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.DAY_OF_MONTH))    // 오늘 날짜 8자리로 표현

    val realm = Realm.getDefaultInstance()    // realm 기본 인스턴스 얻기기


    private val geofenceCountDownTimer:CountDownTimer = object : CountDownTimer()    // 목표 시간동안 카운트 다운을 진행할 변수

   override fun onReceive(context: Context?, intent: Intent?) {

        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e("GeofenceBR",errorMessage)
            return
        }

        // 발생 이벤트 타입
        val geofenceTransition = geofencingEvent.geofenceTransition

        // 이벤트에 따른 행동 코딩
        if (appState == "Start") {

            realm.beginTransaction()    // realm 트랜잭션 시장

            // 지오펜싱 안으로 사용자가 들어올때
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.d(TAG, "사용자가 지오펜싱 진입")
            }

            // 지오펜싱 밖으로 사용자가 나갈떄
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            }

            // 지오펜싱 안에 있을 경우우
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

            }

        }

    }

    // 날짜를 원하는 8자리로 만들어주는 함수
    fun getDate(year : Int , month : Int , day : Int) : String {

        var date : String

        if (month < 10) {
            date =
                year.toString() + '0' + month.toString() + day.toString()
        } else {
            date =
                year.toString() + month.toString() + day.toString()
        }
        return date
    }

}