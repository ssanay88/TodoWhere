package com.example.todowhere

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todowhere.databinding.ActivityAddTodoBinding
import com.example.todowhere.databinding.ActivityMainBinding
import com.naver.maps.map.overlay.CircleOverlay
import io.realm.Realm
import io.realm.kotlin.where
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    // 오늘 날짜로 캘린더 객체 생성
    val calendar: Calendar = Calendar.getInstance()
    var TAG: String = "로그"

    // realm 사용을 위한 객체 선언
    val realm = Realm.getDefaultInstance()

    // 년도, 월, 일 변수를 선언
    var selected_year = calendar.get(Calendar.YEAR)
    var selected_month = calendar.get(Calendar.MONTH) + 1
    var selected_day = calendar.get(Calendar.DAY_OF_MONTH)
    var selected_date: String =
        selected_year.toString() + selected_month.toString() + selected_day.toString()



    // real DB에서 사용할 id를 위한 현재 시간 변수
    var cur_time = Date().time
    var cur_time_form: String = SimpleDateFormat("HHmmss").format(cur_time)!! // 현재 시간을 원하는 형태로 변경



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)



        // 리사이클러뷰 관련 선언
        // MyAdapter를 생성 후 recyclerview의 adapter로 선언해줍니다.
        val myAdapter = MyAdapter(this,find_Item_Count(selected_date))
        mainBinding.TodoRecyclerView.adapter = myAdapter

        // layout을 생성 후 recyclerview의 adapter로 선언해줍니다.
        val layout = LinearLayoutManager(this)
        mainBinding.TodoRecyclerView.layoutManager = layout

        // 어댑터에 넘겨줄 현재 날짜 (캘린더에서 날짜 미선택시)
        // myAdapter.Selected_date = selected_date

        selected_date = getDate(selected_year,selected_month,selected_day)


        // 캘린더뷰에서 날짜 선택 시 날짜 지정
        mainBinding.CalendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->

            // 날짜 선택 시 선택한 시간으로 갱신
            cur_time = Date().time
            cur_time_form = SimpleDateFormat("HHmmss").format(cur_time)

            // 날짜 선택시 선택한 날짜로 갱신
            selected_month = month + 1
            selected_year = year
            selected_day = dayOfMonth

            selected_date = getDate(selected_year,selected_month,selected_day)

//            // 어댑터에 넘겨줄 선택된 날짜
//            myAdapter.Selected_date = selected_date

            Log.d(TAG, "선택한 날짜는 $year - ${month + 1} - $dayOfMonth 입니다.")
            Log.d(TAG, "선택했을때 시간은 $cur_time_form 입니다.")
        }




        // 날짜 선택 후 일정 추가 버튼 클릭 시 yyyyMMdd 형태로 전달
        myAdapter.setonBtnClickListener(object : MyAdapter.onBtnClickListener {

            // onBtnClick 오버라이드 정의
            override fun onBtnClick() {

                Log.d(TAG,"일정 추가 버튼 클릭 !!")

                var next_intent = Intent(this@MainActivity, AddTodoActivity::class.java).apply {
                    // 선택한 날짜 넘겨주기
                    Log.d(TAG,"선택한 날짜 : $selected_date")
                    Log.d(TAG,"선택한 시간 : $cur_time_form")

                    putExtra("DATE", selected_date)
                    putExtra("TIME", cur_time_form)

                }
                startActivity(next_intent)
            }
        })


        // Realm 데이터 베이스
        // val realm = Realm.getDefaultInstance()  // Realm 객체 초기화


    }

    // 날짜를 원하는 8자리로 만들어주는 함수수
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

    // mainActivity에서 선언된 날짜에 해당하는 id를 가진 값들 반환
    fun find_Item_Count(date : String) : Int {

        val realmResult = realm.where<Todo>().contains("id",date).findAll()
        Log.d(ContentValues.TAG," 지금 아이템 수 : ${realmResult.size}")
        return realmResult.size

    }
}