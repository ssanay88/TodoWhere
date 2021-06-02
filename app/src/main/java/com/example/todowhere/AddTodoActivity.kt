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
import kotlin.math.min

class AddTodoActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var addTodoBinding: ActivityAddTodoBinding

    private lateinit var naverMap: NaverMap     // 네이버 맵 사용을 위한 선언

    val realm = Realm.getDefaultInstance()  // 인스턴스 얻기
    val calendar : Calendar = Calendar.getInstance()    // 캘린더 인스턴스 얻기


    // var selected_year = calendar.get(Calendar.YEAR)
    // var selected_month = calendar.get(Calendar.MONTH)
    // var selected_day = calendar.get(Calendar.DAY_OF_MONTH)

    var selected_date = calendar.timeInMillis.toString()

    var goal_time = 0       // 목표 시간


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addTodoBinding = ActivityAddTodoBinding.inflate(layoutInflater)
        val view = addTodoBinding.root
        setContentView(view)


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


    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()   // 인스턴스 해제
    }


    // NaverMap 객체가 준비되면 onMapReady() 콜백 메서드가 호출됩니다.
    override fun onMapReady(p0: NaverMap) {
        // 마커 객체 생성 및 지도에 추가
        val marker = Marker()


        // 지도가 클릭되면 onMapClick() 콜백 메서드가 호출되며, 파라미터로 클릭된 지점의 화면 좌표와 지도 좌표가 전달됩니다.
        naverMap.setOnMapClickListener { pointF, coord ->
            marker.position = LatLng( coord.latitude , coord.longitude )
            marker.map = naverMap
            // coord.latitude , coord.longitude -> 클릭을 통한 선택 지점의 좌표

        }
    }



    // Realm DB에 데이터 추가
    private fun insertTodo() {

        realm.beginTransaction()    // 트랜잭션 시작

        // 객체 생성
        val newItem = realm.createObject<Todo>("selected_date")
        // 값 설정
        newItem.what = addTodoBinding.whatTodo.editText?.text.toString()
        // 캘린더에서 받아온 날짜 넣어주기
        newItem.time = goal_time.toLong()
        // 지도에서 받아온 주소 넣어주기
         newItem.where = "우리집"

        realm.commitTransaction()   // 트랜잭션 종료 반영

    }




}