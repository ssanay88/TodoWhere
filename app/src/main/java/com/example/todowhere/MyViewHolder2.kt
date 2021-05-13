package com.example.todowhere

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.example.todowhere.databinding.AddTodoBinding

class MyViewHolder2(val binding: AddTodoBinding) : RecyclerView.ViewHolder(binding.root) {

    val TAG : String = "로그"

    private val addBtn = binding.todoAddButton

    init {
        Log.d(TAG,"MyViewHolder2 called!!")
    }

}