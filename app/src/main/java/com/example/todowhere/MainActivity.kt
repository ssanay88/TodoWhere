package com.example.todowhere

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.realm.Realm
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    // Realm 데이터 베이스
    val realm = Realm.getDefaultInstance()  // Realm 객체 초기화


    // 오늘 날짜로 캘린더 객체 생성
    val calendar: Calendar = Calendar.getInstance()

    val time : Long = calendar.timeInMillis
    var selected_year = calendar.get(Calendar.YEAR)
    var selected_month = calendar.get(Calendar.MONTH)
    var selected_day = calendar.get(Calendar.DAY_OF_MONTH)





}