package com.example.todowhere

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.todowhere.databinding.AddTodoBinding

// 일정을 생성하고 싶을 경우 사용하는 레이아웃과 연결

//class MyViewHolder2(val binding: AddTodoBinding) : RecyclerView.ViewHolder(binding.root) {
//
//    val TAG : String = "로그"
//
//    private val addBtn = binding.todoAddButton
//
//    init {
//        Log.d(TAG,"MyViewHolder2 called!!")
//    }
//
//    fun bind() {
//        addBtn.setOnClickListener {
//            Log.d(TAG,"일정 추가 버튼 클릭")
//
//            // 일정 추가 버튼 클릭 시 add_todo 파일로 가서 작성
//
//
//        }
//    }
//
//}

class MyViewHolder2(view:View) : RecyclerView.ViewHolder(view) {

    val TAG: String = "로그"

    private val addBtn: Button = view.findViewById(R.id.todo_add_button)

    init {
        Log.d(TAG, "MyViewHolder2 called!!")
    }

    fun bind() {
        addBtn.setOnClickListener {
            Log.d(TAG, "일정 추가 버튼 클릭")

            // 일정 추가 버튼 클릭 시 add_todo 파일로 가서 작성
            val intent : Intent = Intent(MyApplication.ApplicationContext(), AddTodoActivity::class.java)



        }

    }
}