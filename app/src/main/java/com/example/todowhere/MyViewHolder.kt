package com.example.todowhere

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.todowhere.databinding.TodoListBinding
import io.realm.Realm

class MyViewHolder1(val binding: TodoListBinding) : RecyclerView.ViewHolder(binding.root) {

    // Realm 인스턴스
    val realm = Realm.getDefaultInstance()


    val TAG : String = "로그"

    private val TodoTextView = binding.todoText
    private val TimerBtn = binding.timerButton
    private val MapBtn = binding.mapButton
    private val delBtn = binding.deleteButton

    init {
        Log.d(TAG,"MyViewHolder1 called!!")
    }

    fun bind() {

    }



}