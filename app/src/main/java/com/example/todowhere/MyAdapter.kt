package com.example.todowhere

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.todowhere.databinding.AddTodoBinding
import com.example.todowhere.databinding.TodoListBinding
import io.realm.Realm
import io.realm.kotlin.where
import org.w3c.dom.Text

// 생성자에서 Item은 선택된 날짜별로 표시할 할일들의 수
class MyAdapter(private val context: Context, var Item : Int, var todo_datas : List<Todo>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    val realm = Realm.getDefaultInstance()


    // xml을 여러개 사용하려고 할 때
    override fun getItemViewType(position: Int): Int {

        return todo_datas[position].view_type

    }

    // 뷰홀더가 생성될때
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // 연결할 레이아웃 설정
        val view : View?
        return when(viewType) {
            // 일정이 없는 경우 일정 추가 버튼만 출력
            0 -> {
                view = LayoutInflater.from(context).inflate(R.layout.add_todo, parent, false)
                MyViewHolder_Add(view)
            }
            // 일정을 정리하여 보여주는 뷰홀더
            else -> {
                view = LayoutInflater.from(context).inflate(R.layout.todo_list, parent, false)
                MyViewHolder_Update(view)
            }

        }

    }

    // 뷰홀더가 재활용 됐을 때때
   override fun onBindViewHolder(holder: RecyclerView.ViewHolder , position: Int) {
        when(todo_datas[position].view_type) {
            0 -> {
                (holder as MyViewHolder_Add).bind()
            }
            else -> {
                (holder as MyViewHolder_Update).bind(todo_datas[position])
            }

        }

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
        // 해당 요일 별로 realm에서 불러와서 카운트 + 1 -> 마지막은 일정 추가 버튼
        return Item

    }



    // 뷰 홀더 클래스 - 등록된 일정을 보여줄 뷰 홀더
    inner class MyViewHolder_Update(view: View) : RecyclerView.ViewHolder(view) {

        val TAG : String = "로그"

        private val TodoTextView : TextView = view.findViewById(R.id.todoText)
        private val TimerBtn : Button = view.findViewById(R.id.timer_button)
        private val MapBtn : ImageButton = view.findViewById(R.id.map_button)
        private val delBtn : ImageButton = view.findViewById(R.id.delete_button)

        init {
            Log.d(TAG,"MyViewHolder_Update called!!")
        }

        fun bind(item:Todo) {
            // Realm 에서 데이터 불러와서 적용
            TodoTextView.text = item.what
            TimerBtn.text = item.time.toString()
            MapBtn.setOnClickListener {
                Log.d(TAG,"지도 버튼 클릭")
            }
            delBtn.setOnClickListener {
                Log.d(TAG,"삭제 버튼 클릭")
            }


        }

    }

    // 뷰 홀더 클래스 - 일정 추가하고 싶을 경우 사용하는 뷰홀더
    inner class MyViewHolder_Add(view: View) : RecyclerView.ViewHolder(view) {

        val TAG: String = "로그"

        val addBtn: Button = view.findViewById(R.id.todo_add_button)

        init {
            Log.d(TAG, "MyViewHolder_Add called!!")
        }

        fun bind() {
            // 추가 버튼 클릭 시 작동
            addBtn.setOnClickListener {
                Log.d(TAG,"뷰 홀더에서 클릭 이벤트!!")
                listener?.onBtnClick()
            }

        }

    }



}