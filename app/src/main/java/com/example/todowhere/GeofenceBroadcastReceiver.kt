package com.example.todowhere

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where
import java.util.*
import kotlin.concurrent.timer

// appState : 일정 측정을 시작했는지 확인하는 변수 , todayTodo : 오늘 날짜에 해당하는 DB만 리스트로 가져옴
// TODO 에러 ) 브로드캐스트리시버에는 변수를 넣어서 정의하면 안된다!!
class GeofenceBroadcastReceiver(var appState:String) : BroadcastReceiver() {

    var TAG: String = "로그"


    var progressingGeofences : MutableList<Geofence> = mutableListOf()

    val calendar: Calendar = Calendar.getInstance()    // 오늘 날짜로 캘린더 객체 생성
    var today_date = getDate(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )    // 오늘 날짜 8자리로 표현

    val realm = Realm.getDefaultInstance()    // realm 기본 인스턴스 얻기기

    private var timerTask: Timer? = null    // 타이머 사용을 위한 타이머 태스크 선언언


    // private val geofenceCountDownTimer:CountDownTimer = object : CountDownTimer()    // 목표 시간동안 카운트 다운을 진행할 변수

    override fun onReceive(context: Context, intent: Intent) {

        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e("GeofenceBR", errorMessage)
            return
        }

        // 발생 이벤트 타입
        val geofenceTransition = geofencingEvent.geofenceTransition


        // 이벤트에 따른 행동 코딩
        // 계획 측정이 ON인 상태에서만 실행
        if (appState == "Start") {

            // 지오펜싱 안으로 사용자가 들어올때 혹은 진입해있는 경우 -> 타이머 실행
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                Log.d(TAG, "사용자가 지오펜싱 진입")
                // 지오펜싱 이벤트가 발생한 모든 Geofence들
                val triggeringGeofences = geofencingEvent.triggeringGeofences

                // 진행되고 있는 지오펜스에 추가
                triggeringGeofences.forEach {
                    if (progressingGeofences.contains(it)) {
                        Log.d(TAG,"포함된 지오펜싱입니다. ID : ${it.requestId}")
                    } else {
                        progressingGeofences.add(it)
                    }
                }

                // 1초마다 진행할 것
                timerTask = kotlin.concurrent.timer(period = 1000) {
                    // 진행 중인 지오펜싱 리스트 중에서 하나씩 시간 감소
                    progressingGeofences.forEach {
                        realm.beginTransaction()    // realm 트랜잭션 시작

                        var realmResult =
                            realm.where<Todo>().contains("id", it.requestId).findFirst()
                        // 해당 realm 데이터의 시간 감소
                        realmResult!!.time -= 1

                        realm.commitTransaction()   // realm 트랜잭션 종료
                    }
                }
            }

            // 지오펜싱 밖으로 사용자가 나갈떄 -> 타이머 중지지
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Log.d(TAG, "사용자가 지오펜싱 진출")

                val triggeringGeofences = geofencingEvent.triggeringGeofences

                triggeringGeofences.forEach {
                    if (it in progressingGeofences) {
                        progressingGeofences.remove(it)
                    } else {
                        // 삭제할 Geofence 없음
                    }
                }

            }

        }    // appState가 Start일 경우에만 진행
        else {
            timerTask?.cancel()    // appState가 Stop일 경우 타이머 종료
        }

    }


    // 날짜를 원하는 8자리로 만들어주는 함수
    private fun getDate(year: Int, month: Int, day: Int): String {

        var date: String

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
