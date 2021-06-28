package com.example.todowhere

import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import com.example.todowhere.databinding.ActivityAddTodoBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import io.realm.Realm
import io.realm.kotlin.createObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

class AddTodoActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = "로그"

    private lateinit var addTodoBinding: ActivityAddTodoBinding

    private lateinit var naverMap: NaverMap     // 네이버 맵 사용을 위한 선언

    val realm = Realm.getDefaultInstance()  // 인스턴스 얻기
    // val calendar : Calendar = Calendar.getInstance()    // 캘린더 인스턴스 얻기

    var goal_time = 0       // 목표 시간

    lateinit var now_date : String
    lateinit var now_time : String


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        addTodoBinding = ActivityAddTodoBinding.inflate(layoutInflater)
        val view = addTodoBinding.root
        setContentView(view)



        // MainActivity 에서 인텐트를 전달받기 위해 선언
        var intent_from_mainactivity = getIntent()

        // 전달 받은 날짜와 시간 저장
        now_date = intent_from_mainactivity.getStringExtra("DATE")!!
        now_time = intent_from_mainactivity.getStringExtra("TIME")!!


        // 네이버 지도 프래그먼트
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.mapView) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.mapView, it).commit()

            }

        // OnMapReadyCallBack을 상속받은 뒤 this 사용
        // getMapAsync를 호출하여 비동기로 onMapReady 콜백 메서드 호출
        // onMapReady에서 NaverMap 객체를 받음
        mapFragment.getMapAsync(this)

        Log.d(TAG,"AddTodo 액티비티 시작!!")



        // 타임피커 완성 , 목표 시간 goal_time 변수에 저장
        addTodoBinding.TimeButton.setOnClickListener {

            val cal = Calendar.getInstance()

            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                goal_time = (hour.toString()+ minute.toString()).toInt()
                addTodoBinding.TimeButton.text = SimpleDateFormat("HH:mm").format(cal.time)
            }

            TimePickerDialog(this,
                timeSetListener,
                0,
                0,
                true)
                .show()
        }


        // 등록 버튼 클릭 시 해당 데이터 등록
        addTodoBinding.AddButton.setOnClickListener {
            insertTodo()
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()   // 인스턴스 해제
    }


    // NaverMap 객체가 준비되면 onMapReady() 콜백 메서드가 호출됩니다.
    override fun onMapReady(naverMap : NaverMap) {
        // 마커 객체 생성 및 지도에 추가
        val marker = Marker()

        // 지도가 클릭되면 onMapClick() 콜백 메서드가 호출되며, 파라미터로 클릭된 지점의 화면 좌표와 지도 좌표가 전달됩니다.
        naverMap.setOnMapClickListener { pointF, coord ->
            marker.position = LatLng( coord.latitude , coord.longitude )
            marker.map = naverMap
            // coord.latitude , coord.longitude -> 클릭을 통한 선택 지점의 좌표

        }
    }

    // 06.21 입력 받은 날짜를 기준으로 id 생성 후 입력 받은 값들 realm DB 등록해주기

    // Realm DB에 데이터 추가
    private fun insertTodo() {

        realm.beginTransaction()    // 트랜잭션 시작

        // 객체 생성
        // id는 캘린더에서 선택한 날짜와 당시 시간으로 설정정
       val newItem = realm.createObject<Todo>(now_date + now_time)
        // 값 설정
        newItem.what = addTodoBinding.whatTodo.editText?.text.toString()
        // 캘린더에서 받아온 날짜 넣어주기
        newItem.time = goal_time.toLong()
        // 지도에서 받아온 주소 넣어주기
        newItem.where = "우리집"

        realm.commitTransaction()   // 트랜잭션 종료 반영

    }


}