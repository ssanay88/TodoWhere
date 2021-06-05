package com.example.todowhere

import android.util.Log
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.todowhere.databinding.TodoListBinding
import io.realm.Realm
import org.jetbrains.anko.find
import org.w3c.dom.Text

// todo_list.xml 파일과 연결 즉, 만들어진 계획을 보여주는 레이아웃
//class MyViewHolder1(val binding: TodoListBinding) : RecyclerView.ViewHolder(binding.root) {
//
//    // Realm 인스턴스
//    val realm = Realm.getDefaultInstance()
//
//
//    val TAG : String = "로그"
//
//    private val TodoTextView = binding.todoText
//    private val TimerBtn = binding.timerButton
//    private val MapBtn = binding.mapButton
//    private val delBtn = binding.deleteButton
//
//    init {
//        Log.d(TAG,"MyViewHolder1 called!!")
//    }
//
//    fun bind() {
//
//    }
//
//}

class MyViewHolder1(view: View) : RecyclerView.ViewHolder(view) {

    // Realm 인스턴스
    val realm = Realm.getDefaultInstance()


    val TAG : String = "로그"

    private val TodoTextView : Text = view.findViewById(R.id.todoText)
    private val TimerBtn : Button = view.findViewById(R.id.TimeButton)
    private val MapBtn : Button = view.findViewById(R.id.map_button)
    private val delBtn : Button = view.findViewById(R.id.delete_button)

    init {
        Log.d(TAG,"MyViewHolder1 called!!")
    }

    fun bind() {

    }

}