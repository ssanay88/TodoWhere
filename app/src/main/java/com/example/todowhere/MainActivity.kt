package com.example.todowhere

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todowhere.databinding.ActivityMainBinding
import io.realm.Realm
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    // 오늘 날짜로 캘린더 객체 생성
    val calendar: Calendar = Calendar.getInstance()
    var TAG : String = "로그"

    // 년도, 월, 일 변수를 선언
    var selected_year = calendar.get(Calendar.YEAR)
    var selected_month = calendar.get(Calendar.MONTH)
    var selected_day = calendar.get(Calendar.DAY_OF_MONTH)
    var selected_date = SimpleDateFormat("yyyy/MM/dd").format(calendar)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainBinding  =  ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)


        // 캘린더뷰에서 날짜 선택 시 날짜 지정정
       mainBinding.CalendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->

            selected_day = dayOfMonth
            selected_month = month+1
            selected_year = year
            Log.d(TAG,"오늘 날짜는 $year - ${month+1} - $dayOfMonth 입니다.")
        }

        // 06.02 날짜를 yyyy/MM/dd 형태로 다음 인텐트로 전달하기

        mainBinding.TodoRecyclerView.adapter = MyAdapter()
        mainBinding.TodoRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    // Realm 데이터 베이스
    val realm = Realm.getDefaultInstance()  // Realm 객체 초기화



}