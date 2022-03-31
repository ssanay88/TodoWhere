package com.example.todowhere

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.todowhere.RealmDB.Todo
import io.realm.Realm
import io.realm.kotlin.where
import java.util.*

// 매일 리셋해야하는 행동
// 1. 완료하지 못한 할 일 들 모두 'Finish' 처리
// 2. Todo리스트와 지오펜싱 리스트 클리어 이후 다음날짜의 Todo와 지오펜싱 추가
class ResetBroadcastReceiver: BroadcastReceiver() {

    val realm = Realm.getDefaultInstance()
    // 오늘 날짜로 캘린더 객체 생성
    val calendar: Calendar = Calendar.getInstance()


    var today_year = calendar.get(Calendar.YEAR)
    var today_month = calendar.get(Calendar.MONTH) + 1
    var today_day = calendar.get(Calendar.DAY_OF_MONTH)

    override fun onReceive(context: Context, intent: Intent) {

        val yesterday = intent.getStringExtra("yesterdayDate")

        if (yesterday != null) {
            todoAllFinished(yesterday)    // 지나간 할 일들 모두 Finish처리
        }
        todayTodoAdd()    // 다음날의 할 일들 추가

    }


    private fun todoAllFinished(date: String) {
        realm.beginTransaction()

        var todoResult = realm.where<Todo>().contains("id",date).findAll()
        todoResult.forEach {
            if (it.state != "Done") {
                it.state = "Finish"
            }
        }

        realm.commitTransaction()

    }


    private fun todayTodoAdd() {

    }


}