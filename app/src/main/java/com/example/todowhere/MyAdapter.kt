package com.example.todowhere

import android.app.AlertDialog
import android.content.ContentValues
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
        var parent_context = parent.context

        return when(viewType) {
            // 일정이 없는 경우 일정 추가 버튼만 출력
            0 -> {
                view = LayoutInflater.from(context).inflate(R.layout.add_todo, parent, false)
                MyViewHolder_Add(view)
            }
            // 일정을 정리하여 보여주는 뷰홀더
            else -> {
                view = LayoutInflater.from(context).inflate(R.layout.todo_list, parent, false)
                MyViewHolder_Update(view , parent_context)
            }

        }

    }

    // 뷰홀더가 재활용 됐을 때
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
    interface onAddBtnClickListener {
        fun onAddBtnClick()    // 클릭된 순간 로직을 담을 추상 메소드
    }

    interface onDelBtnClickListener {
        fun onDelBtnClick(todo: Todo)
    }

    // 리스너 선언
    private var addListener : onAddBtnClickListener? = null
    private var delListener : onDelBtnClickListener? = null

    fun setonAddBtnClickListener(listener: onAddBtnClickListener) {
        this.addListener = listener
    }

    fun setonDelBtnClickListener(listener: onDelBtnClickListener) {
        this.delListener = listener
    }





    // 목록에서 보여줄 아이템 개수
    override fun getItemCount(): Int {
        // 해당 요일 별로 realm에서 불러와서 카운트 + 1 -> 마지막은 일정 추가 버튼
        return Item

    }

    // 시간:분 형태로 표시
    private fun HourMin(time:Long) : String {
        var hour : Long = time / 3600
        var min : Long = (time % 3600) / 60

        if (hour < 10 && min < 10) {
            return "0$hour : 0$min"
        } else if (hour < 10 && min >= 10) {
            return "0$hour : $min"
        } else if (hour >= 10 && min < 10) {
            return "$hour : 0$min"
        } else {
            return "$hour : $min"
        }

    }



    // 뷰 홀더 클래스 - 등록된 일정을 보여줄 뷰 홀더
    inner class MyViewHolder_Update(view: View , pContext: Context) : RecyclerView.ViewHolder(view) {

        val TAG : String = "로그"
        val ActivityContext = pContext

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
            TimerBtn.text = HourMin(item.time)    // item.time.toString()


            MapBtn.setOnClickListener {
                Log.d(TAG,"지도 버튼 클릭")
                // 지도 다이어로그 띄우기
                initMapBtnClicked()
            }
            delBtn.setOnClickListener {
                Log.d(TAG,"삭제 버튼 클릭 삭제 ID : ${item.id}")
                // 삭제 과정 확인 다이어로그 Yes -> 해당 목표 DB에서 삭제 및 아이템 재정리
                delListener?.onDelBtnClick(item)

                //initDelBtnClicked(item.id , ActivityContext)

            }


        }

        // 지도 버튼을 클릭 했을을 경우 발생 벤트
        fun initMapBtnClicked() {

        }


        // 삭제 버튼을 클릭 했을 경우 발생 이벤트
        // 10.28 Context 불러오기 검색
        fun initDelBtnClicked(ID:String , ActivityContext:Context) {
            Log.d(TAG, "삭제 버튼 발동")
            // Todo 1. 삭제할건지 다시 묻는 Dialog   2. 삭제 시 DB에서 데이터 삭제 및 아이템 개수 -1
            AlertDialog.Builder(ActivityContext)
                .setTitle("일정 삭제")
                .setMessage("해당 일정을 정말로 삭제하시겠습니까?")
                .setPositiveButton("삭제",{ _,_ ->
                    // Todo DB에서 해당 일정 삭제
                    deleteFromDB(ID)
                    notifyDataSetChanged()

                })
                .setNegativeButton("취소" ,{ _,_ ->
                    // 팝업 닫기
                })
                .show()


        }

        fun deleteFromDB(ID: String) {

            realm.beginTransaction()

            var result = realm.where<Todo>().equalTo("id",ID).findFirst()
            if (result != null) {
                Log.d(ContentValues.TAG, "삭제 result : $result")
                result.deleteFromRealm()
            }

            realm.commitTransaction()

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
                addListener?.onAddBtnClick()
            }

        }

    }






}