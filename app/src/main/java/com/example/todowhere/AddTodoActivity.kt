package com.example.todowhere

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.example.todowhere.databinding.ActivityAddTodoBinding
import io.realm.Realm
import io.realm.kotlin.createObject

class AddTodoActivity : AppCompatActivity() {

    private lateinit var addTodoBinding: ActivityAddTodoBinding

    val realm = Realm.getDefaultInstance()  // 인스턴스 얻기

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_add_todo)
        addTodoBinding = ActivityAddTodoBinding.inflate(layoutInflater)
        val view = addTodoBinding.root
        setContentView(view)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()   // 인스턴스 해제
    }


    private fun insertTodo() {
        realm.beginTransaction()    // 트랜잭션 시작

        // 객체 생성
        val newItem = realm.createObject<Todo>("id")
        // 값 설정
        newItem.what_todo = addTodoBinding.whatTodo.editText?.text.toString()
        // 캘린더에서 받아온 날짜 넣어주기
        // newItem.date = cale
        // 지도에서 받아온 주소 넣어주기
        // newItem.where_todo =

        realm.commitTransaction()   // 트랜잭션 종료 반영

    }

}