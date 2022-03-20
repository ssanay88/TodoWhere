package com.example.todowhere

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.todowhere.RealmDB.Todo
import com.scwang.wave.MultiWaveHeader
import io.realm.Realm
import org.jetbrains.anko.backgroundColor

// 생성자에서 Item은 선택된 날짜별로 표시할 할일들의 수
class MyAdapter(private val context: Context, var Item : Int, var todo_datas : List<Todo>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val TAG = "로그"

    val realm = Realm.getDefaultInstance()

    // 인터페이스
    interface OnAddBtnClickListener {
        fun onAddClick()    // 클릭된 순간 로직을 담을 추상 메소드
    }
    interface OnDelBtnClickListener {
        fun onDelClick(todo: Todo)
    }
    interface OnMapBtnClickListener {
        fun onMapClick(todo: Todo)
    }

    // 리스너 선언
    private var addListener : OnAddBtnClickListener? = null
    private var delListener : OnDelBtnClickListener? = null
    private var mapListener : OnMapBtnClickListener? = null


    fun setOnAddBtnClickListener(addlistener: OnAddBtnClickListener) {

        addListener = addlistener
        Log.d(TAG , "setOnAddBtnClickListener 실행")
    }

    fun setOnDelBtnClickListener(dellistener: OnDelBtnClickListener) {
        delListener = dellistener
    }

    fun setOnMapBtnClickListener(maplistener: OnMapBtnClickListener) {
        mapListener = maplistener
    }


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
                MyViewHolderAdd(view)
            }

            // 일정을 정리하여 보여주는 뷰홀더
            else -> {
                view = LayoutInflater.from(context).inflate(R.layout.todo_list, parent, false)
                MyViewHolderUpdate(view)
            }

        }

    }

    // 뷰홀더가 재활용 됐을 때
   override fun onBindViewHolder(holder: RecyclerView.ViewHolder , position: Int) {
        when(todo_datas[position].view_type) {
            0 -> {
                (holder as MyViewHolderAdd).bind()
            }
            else -> {
                (holder as MyViewHolderUpdate).bind(todo_datas[position])
            }
        }
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

    // 목록에서 보여줄 아이템 개수
    override fun getItemCount(): Int {
        // 해당 요일 별로 realm에서 불러와서 카운트 + 1 -> 마지막은 일정 추가 버튼
        return Item
    }

    // 뷰 홀더 클래스 - 등록된 일정을 보여줄 뷰 홀더
    inner class MyViewHolderUpdate(view: View) : RecyclerView.ViewHolder(view) {

        val TAG : String = "로그"

        private var todoTextView : TextView = view.findViewById(R.id.todoText)
        private var timerTextView : TextView = view.findViewById(R.id.timer_button)
        private var mapBtn : ImageButton = view.findViewById(R.id.map_button)
        private var delBtn : ImageButton = view.findViewById(R.id.delete_button)
        private var doingEffect : MultiWaveHeader = view.findViewById(R.id.doingEffect)

        init {
            Log.d(TAG,"MyViewHolder_Update called!!")
        }

        fun bind(item: Todo) {
            // Realm 에서 데이터 불러와서 적용
            todoTextView.text = item.what
            // 아이템 상태가 완료일 경우 버튼에 완료 표시
            when (item.state) {

                "Doing" -> {
                    timerTextView.text = HourMin(item.time)    // item.time.toString()
                    doingEffect.start()
                }
                "Stop","Wait" -> {
                    timerTextView.text = HourMin(item.time)
                    doingEffect.stop()
                }
                "Done" -> {
                    timerTextView.text = "완료"
                    doingEffect.stop()
                }
                "Finish" -> {
                    timerTextView.text = "실패"
                    doingEffect.stop()
                }



            }



            mapBtn.setOnClickListener {
                // 지도 다이어로그 띄우기
                mapListener?.onMapClick(item)
            }

            delBtn.setOnClickListener {
                Log.d(TAG,"삭제 버튼 클릭 삭제 ID : ${item.id}")
                // 삭제 과정 확인 다이어로그 Yes -> 해당 목표 DB에서 삭제 및 아이템 재정리
                delListener?.onDelClick(item)

            }

        }

    }

    // 뷰 홀더 클래스 - 일정 추가하고 싶을 경우 사용하는 뷰홀더
    inner class MyViewHolderAdd(view: View) : RecyclerView.ViewHolder(view) {

        val TAG: String = "로그"

        private val addBtn: Button = view.findViewById(R.id.todo_add_button)

        init {
            Log.d(TAG, "MyViewHolder_Add called!!")
        }

        fun bind() {
            // 추가 버튼 클릭 시 작동
            addBtn.setOnClickListener {

                if (addListener == null) {
                    Log.d(TAG,"addListener이 null 입니다.")
                } else {
                    addListener?.onAddClick()
                }

            }
        }

    }




}