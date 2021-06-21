package com.example.todowhere

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todowhere.databinding.ActivityAddTodoBinding
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
    var selected_month = calendar.get(Calendar.MONTH) + 1
    var selected_day = calendar.get(Calendar.DAY_OF_MONTH)
    var selected_date : String = selected_year.toString() + selected_month.toString() + selected_day.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainBinding  =  ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        // 리사이클러뷰 관련 선언
        // MyAdapter를 생성 후 recyclerview의 adapter로 선언해줍니다.
        val myAdapter = MyAdapter(this)
        mainBinding.TodoRecyclerView.adapter = myAdapter

        // layout을 생성 후 recyclerview의 adapter로 선언해줍니다.
        val layout = LinearLayoutManager(this)
        mainBinding.TodoRecyclerView.layoutManager = layout


        // 캘린더뷰에서 날짜 선택 시 날짜 지정
       mainBinding.CalendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->

            selected_day = dayOfMonth
            selected_month = month+1
            selected_year = year
            selected_date = selected_year.toString() + selected_month.toString() + selected_day.toString()
            Log.d(TAG,"오늘 날짜는 $year - ${month+1} - $dayOfMonth 입니다.")
        }


        // 날짜 선택 후 일정 추가 버튼 클릭 시 yyyyMMdd 형태로 전달
        myAdapter.setonBtnClickListener(object : MyAdapter.onBtnClickListener{
            // onBtnClick 오버라이드 정의
            override fun onBtnClick() {
                var intent = Intent(this@MainActivity, AddTodoActivity::class.java).apply {
                    // 선택한 날짜 넘겨주기
                    putExtra("DATE", selected_date)
                }
                startActivity(intent)
            }
        })


    // Realm 데이터 베이스
    val realm = Realm.getDefaultInstance()  // Realm 객체 초기화



}