package com.example.todowhere

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.todowhere.databinding.AddTodoBinding
/*
class MyViewHolder2(val binding: AddTodoBinding) : RecyclerView.ViewHolder(binding.root) {

    val TAG : String = "로그"

    private val addBtn = binding.todoAddButton

    init {
        Log.d(TAG,"MyViewHolder2 called!!")
    }

}*/

class MyViewHolder2(view:View) : RecyclerView.ViewHolder(view) {

    val TAG : String = "로그"



    init {
        Log.d(TAG,"MyViewHolder2 called!!")

    }

}