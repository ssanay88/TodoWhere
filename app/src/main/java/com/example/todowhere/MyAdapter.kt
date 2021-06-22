package com.example.todowhere

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.todowhere.databinding.AddTodoBinding
import com.example.todowhere.databinding.TodoListBinding
import io.realm.Realm
import org.w3c.dom.Text

class MyAdapter(private val context: Context) : RecyclerView.Adapter<MyAdapter.MyViewHolder2>() {

    // xml을 여러개 사용하려고 할 때
    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }


    // 뷰홀더가 생성될때
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder2 {
        // 연결할 레이아웃 설정
        val view = LayoutInflater.from(parent.context).inflate(R.layout.add_todo, parent, false)
        return MyViewHolder2(view)

//        val add_todo_binding = AddTodoBinding.inflate(LayoutInflater.from(parent.context),parent,false)
//        return MyViewHolder2(add_todo_binding)

        /*
        val binding = TodoListBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder2(binding)*/

    }

    // 뷰홀더가 재활용 됐을 때때
   override fun onBindViewHolder(holder: MyViewHolder2, position: Int) {

        holder.bind()

    }

    // 인터페이스
    interface onBtnClickListener {
        fun onBtnClick()    // 클릭된 순간 로직을 담을 추상 메소드
    }
    // 리스너 선언
    private var listener : onBtnClickListener? = null

    fun setonBtnClickListener(listener: onBtnClickListener) {
        this.listener = listener
    }

    // 목록에서 보여줄 아이템 개수
    override fun getItemCount(): Int {
        return 3
    }


    // 뷰 홀더 클래스 - 등록된 일정을 보여줄 뷰 홀더
    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

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

    // 뷰 홀더 클래스 - 일정 추가하고 싶을 경우 사용하는 뷰홀더
    inner class MyViewHolder2(view: View) : RecyclerView.ViewHolder(view) {

        val TAG: String = "로그"

        val addBtn: Button = view.findViewById(R.id.todo_add_button)

        init {
            Log.d(TAG, "MyViewHolder2 called!!")
        }

        fun bind() {
            // 추가 버튼 클릭 시 작동
            addBtn.setOnClickListener {
                listener?.onBtnClick()
            }

        }

    }



}