package com.example.todowhere

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todowhere.databinding.AddTodoBinding
import com.example.todowhere.databinding.TodoListBinding

class MyAdapter : RecyclerView.Adapter<MyViewHolder>() {

    // xml을 여러개 사용하고 할 때
    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    // 뷰홀더가 생성될때
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        if ()
        val add_todo_binding = AddTodoBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(add_todo_binding)

        // 연결할 레이아웃 설정
        val binding = TodoListBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)

    }

    // 뷰홀더가 재활용 됐을 때때
   override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

       holder.bind()

    }

    // 목록의 아이템 개수
    override fun getItemCount(): Int {

    }
}