package com.example.todowhere

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.todowhere.RealmDB.Todo
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import io.realm.Realm
import io.realm.kotlin.where
import java.util.*

// appState : 일정 측정을 시작했는지 확인하는 변수 , todayTodo : 오늘 날짜에 해당하는 DB만 리스트로 가져옴
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    var TAG: String = "로그"

    val calendar: Calendar = Calendar.getInstance()    // 오늘 날짜로 캘린더 객체 생성
    var today_date = getDate(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )    // 오늘 날짜 8자리로 표현

    val realm = Realm.getDefaultInstance()    // realm 기본 인스턴스 얻기기

    override fun onReceive(context: Context, intent: Intent) {

        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        Log.d("GeofenceBR", "지오펜싱 연결")
        Toast.makeText(context,"지오펜싱 시작", Toast.LENGTH_SHORT).show()



        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e("GeofenceBR", errorMessage)
            return
        }

        // 발생 이벤트 타입
        val geofenceTransition = geofencingEvent.geofenceTransition


        // 이벤트에 따른 행동 코딩
        // 계획 측정이 ON인 상태에서만 실행


        // 지오펜싱 안으로 사용자가 들어올때 혹은 진입해있는 경우 -> 타이머 실행
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            Log.d(TAG, "사용자가 지오펜싱 진입함")
            // 지오펜싱 이벤트가 발생한 모든 Geofence들
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            // 진행되고 있는 지오펜스에 추가
            triggeringGeofences.forEach {
                // Enter Or Dwell인 경우 DB의 상태 진행중으로 변경
                realm.beginTransaction()

                var AllrealmResult = realm.where<Todo>().contains("id",today_date).findAll()
                Log.d(TAG,"전체 realm : $AllrealmResult")

                // 왜 못찾는가 -> 아이디가 다르게 저장되고 있다
                var realmResult = realm.where<Todo>().equalTo("id", it.requestId).findFirst()
                realmResult?.state = "Doing"    // 진행중으로 변경
                Log.d(TAG,"실행 ID : ${it.requestId}")
                Log.d(TAG,"해당 realm : $realmResult")
                realm.commitTransaction()
                }
            }

            // 지오펜싱 밖으로 사용자가 나갈떄 -> 타이머 중지지
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Log.d(TAG, "사용자가 지오펜싱 벗어남")

                val triggeringGeofences = geofencingEvent.triggeringGeofences

                triggeringGeofences.forEach {
                    // Enter Or Dwell인 경우 DB의 상태 진행중으로 변경
                    realm.beginTransaction()
                    var realmResult = realm.where<Todo>().contains("id", it.requestId.toString()).findFirst()
                    realmResult?.state = "Stop"    // 진행중으로 변경
                    Log.d(TAG,"정지 ID : ${it.requestId}")
                    Log.d(TAG,"해당 realm : $realmResult")
                    realm.commitTransaction()
                }
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
