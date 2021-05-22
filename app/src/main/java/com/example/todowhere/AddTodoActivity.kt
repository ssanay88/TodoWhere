package com.example.todowhere

import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.example.todowhere.databinding.ActivityAddTodoBinding
import io.realm.Realm
import io.realm.kotlin.createObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class AddTodoActivity : AppCompatActivity() {

    private lateinit var addTodoBinding: ActivityAddTodoBinding

    val realm = Realm.getDefaultInstance()  // 인스턴스 얻기
    val calendar : Calendar = Calendar.getInstance()    // 캘린더 인스턴스 얻기

    // var selected_year = calendar.get(Calendar.YEAR)
    // var selected_month = calendar.get(Calendar.MONTH)
    // var selected_day = calendar.get(Calendar.DAY_OF_MONTH)

    var selected_date = calendar.timeInMillis.toString()

    var goal_time = 0       // 목표 시간


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addTodoBinding = ActivityAddTodoBinding.inflate(layoutInflater)
        val view = addTodoBinding.root
        setContentView(view)

        // 타임피커 완성 , 목표 시간 goal_time 변수에 저장
        addTodoBinding.TimeButton.setOnClickListener {

            val cal = Calendar.getInstance()

            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                goal_time = (hour.toString()+ minute.toString()).toInt()
                addTodoBinding.TimeButton.text = SimpleDateFormat("HH:mm").format(cal.time)
            }

            TimePickerDialog(this,
                timeSetListener,
                0,
                0,
                true)
                .show()
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()   // 인스턴스 해제
    }


    private fun insertTodo() {
        realm.beginTransaction()    // 트랜잭션 시작


        // 객체 생성
        val newItem = realm.createObject<Todo>("selected_date")
        // 값 설정
        newItem.what = addTodoBinding.whatTodo.editText?.text.toString()
        // 캘린더에서 받아온 날짜 넣어주기
        newItem.time = goal_time.toLong()
        // 지도에서 받아온 주소 넣어주기
        // newItem.where_todo =

        realm.commitTransaction()   // 트랜잭션 종료 반영

    }




}