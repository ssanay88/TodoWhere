package com.example.todowhere.Activity

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color.GREEN
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.todowhere.BuildConfig
import com.example.todowhere.DTO.GetAllDto
import com.example.todowhere.R
import com.example.todowhere.RealmDB.Geofencing
import com.example.todowhere.service.ReverseGeocodingService
import com.example.todowhere.RealmDB.Todo
import com.example.todowhere.databinding.ActivityAddTodoBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import io.realm.Realm
import io.realm.kotlin.createObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class AddTodoActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = "로그"

    private lateinit var addTodoBinding: ActivityAddTodoBinding

    private lateinit var naverMap: NaverMap     // 네이버 맵 사용을 위한 선언
    private lateinit var reverseGeocodingService: ReverseGeocodingService    // reverse geocoding 서비스

    private var userLocationManager : LocationManager? = null    // 현재 위치 받아오기용 LocationManager
    private var userLocationListener : LocationListener? = null    // 위치 감지 Listener

    val realm = Realm.getDefaultInstance()  // 인스턴스 얻기

    var goal_time = 0       // 목표 시간

    lateinit var now_date : String  // 선택된 날짜
    lateinit var now_time : String  // 선택했을 시간 - id용

    var selected_Lat : Double = 0.0   // 선택한 좌표의 위도
    var selected_Lng : Double = 0.0   // 선택한 좌표의 경도

    // 현재 좌표를 위한 locationSource 선언
    private lateinit var locationSource: FusedLocationSource

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        addTodoBinding = ActivityAddTodoBinding.inflate(layoutInflater)
        val view = addTodoBinding.root
        setContentView(view)

        // 현재 좌표값 불러오기
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        // MainActivity 에서 인텐트를 전달받기 위해 선언
        var intent_from_mainactivity = getIntent()

        // retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://naveropenapi.apigw.ntruss.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        reverseGeocodingService = retrofit.create(ReverseGeocodingService::class.java)


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
                // 목표 시간 , 초 단위로 진행
                goal_time = (hour * 3600 + minute * 60)
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
            if (insertTodo() == true) {
                insertGeofencing()    // 지오펜싱도 추가
                var next_intent = Intent(this, MainActivity::class.java).apply {
                    // 좌표의 위도, 경도 전송
//                    putExtra("Lat", selected_Lat)
//                    putExtra("Lng", selected_Lng)
//                    putExtra("Time", goal_time)
                }
                startActivity(next_intent)
            } else {
                // 아무 실행 X
            }

        }

        // 취소 버튼 클릭 시
        addTodoBinding.CancelButton.setOnClickListener {
            super.onBackPressed()
        }

        // MAP 버튼 클릭 시 위치 등록
        addTodoBinding.MapButton.setOnClickListener {
            if ( selected_Lng != 0.0 && selected_Lat != 0.0) {
                // 주소로 변경 - 네이버 reverse geocoding API 사용
                Log.d(TAG,"좌표값 입력 Lng : $selected_Lng , Lat : $selected_Lat")

                reverseGeocodingService.getGeocoding(
                    BuildConfig.REVESEGEOCODING_API_KEY_ID,
                    BuildConfig.REVESEGEOCODING_API_KEY,
                    "$selected_Lng,$selected_Lat")
                    .enqueue(object : Callback<GetAllDto> {

                        override fun onResponse(
                            call: Call<GetAllDto>,
                            response: Response<GetAllDto>
                        ) {
                            if (response.isSuccessful.not()) {
                                Log.e(TAG,"응답에 실패했습니다.")
                                return
                            }

                            response.body().let {
                                // TODO 응답은 했지만 NULL 출력
                                Log.d(TAG,"${it?.status?.code} , ${it?.status?.name}")
                                it?.results?.forEach {
                                    Log.d(TAG,it.name)
                                    Log.d(TAG,"${it.region.area1.name} , ${it.region.area2.name} , ${it.land.name} , ${it.land.number1} ,${it.land.addition0.value}")
                                    addTodoBinding.locationTextView.text =
                                        it.region.area1.name + it.region.area2.name + it.land.name + it.land.number1 + it.land.addition0.value

                                }

//                                Log.d(TAG,"${it?.result?.results.region?.area1?.name} , ${it?.result?.region?.area2?.name} ,${it?.result?.region?.area3?.name}")
                            }

                        }

                        override fun onFailure(call: Call<GetAllDto>, t: Throwable) {
                            Log.e(TAG,t.toString())
                        }

                    })
                // TODO 받아온 데이터 처리

            }
            else {
                Toast.makeText(this, "지도에서 목적지를 선택하여 주십시오", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()   // 인스턴스 해제
    }


    // NaverMap 객체가 준비되면 onMapReady() 콜백 메서드가 호출됩니다.
    override fun onMapReady(naverMap : NaverMap) {

        this.naverMap = naverMap
        naverMap.minZoom = 11.0
        naverMap.maxZoom = 18.0


        // 현재 위치를 받아온다.
        getUserLocation(naverMap)

        var checkFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        var checkCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (checkFineLocationPermission == PackageManager.PERMISSION_GRANTED && checkCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            userLocationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                30f,
                userLocationListener!!
            )
        } else {
            Toast.makeText(this, "위치 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
        }

        // 마지막 위치를 반환이지만 아직 위치 수신 전이면 null을 반환
        if (locationSource.lastLocation == null) {
            selected_Lat = 0.0
            selected_Lng = 0.0
        } else {
            selected_Lat = locationSource.lastLocation!!.latitude
            selected_Lng = locationSource.lastLocation!!.longitude
        }




    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

            if (locationSource.onRequestPermissionsResult(requestCode,permissions,grantResults)) {
                if (!locationSource.isActivated) {
                    Toast.makeText(this,"권한을 허락해주십시오",Toast.LENGTH_LONG)
                    naverMap.locationTrackingMode = LocationTrackingMode.None
                }
                return
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }


    // 06.21 입력 받은 날짜를 기준으로 id 생성 후 입력 받은 값들 realm DB 등록해주기

    // 입력 값들이 올바르게 들어가 있는지 확인
    private fun checkPlan() : Boolean {
        // 할 일 텍스트 체크
        if (addTodoBinding.whatTodoText.text.toString().isBlank()) {
            Toast.makeText(this,"해야하는 일을 입력하여 주세요",Toast.LENGTH_SHORT).show()
            return false
        }
        // 목표 시간 체크
        else if (goal_time == 0) {
            Toast.makeText(this,"목표 시간을 입력하여 주세요",Toast.LENGTH_SHORT).show()
            return false
        }
        // 목표 위치 체크
        else if (selected_Lat == 0.0 || selected_Lng == 0.0) {
            Toast.makeText(this,"목표 위치를 선택하여 주세요",Toast.LENGTH_SHORT).show()
            return false
        }

        else {
            return true
        }



    }
    // Realm DB에 데이터 추가
    private fun insertTodo() : Boolean {

        if (checkPlan() == false) {
            // 다시 계획이 올바르게 들어갔는지 확인
            return false
        }
        else
        {
            realm.beginTransaction()    // 트랜잭션 시작

            // 객체 생성
            // id는 캘린더에서 선택한 날짜와 당시 시간으로 설정
            val newItem = realm.createObject<Todo>(now_date + now_time)
            // 무엇을 할지 설정
            newItem.what = addTodoBinding.whatTodo.editText?.text.toString()
            // 목표 달성 시간 넣어주기
            newItem.time = goal_time.toLong()
            // 지도에서 받아온 목표 범위 넣어주기 , 0701 Realm DB에 LatLngBounds는 사용 불가 -> 중심 좌표를 등록하는것으로 정리
            newItem.center_lat = selected_Lat
            newItem.center_lng = selected_Lng
            // 리사이클뷰에서 보여줄 뷰홀더 변경
            newItem.view_type = 1
            // 계획 상황 설정
            newItem.state = "Wait"



            Log.d(
                TAG,
                "ID : ${now_date + now_time}  // Todo : ${newItem.what}  // Time : ${newItem.time} "
            )

            realm.commitTransaction()   // 트랜잭션 종료 반영
            return true
        }

    }

    // Realm DB에 Geofencing을 추가
    private fun insertGeofencing() {

        add_geofence_DB(now_date+now_time,selected_Lat,selected_Lng)

    }

    // 새로 생성된 지오펜싱 데이터를 DB에 추가
    private fun add_geofence_DB(geofencingId: String, savedLat: Double, savedLng: Double) {

        realm.beginTransaction()    // 트랜잭션 시작

        // 객체 생성
        val newGeofencing = realm.createObject<Geofencing>(geofencingId)
        newGeofencing.lat = savedLat    // 위도 설정
        newGeofencing.lng = savedLng    // 경도 설정

        realm.commitTransaction()    // 트랜잭션 종료 및 반영영
    }

    // 유저의 현재 위치를 반환하는 함수
    private fun getUserLocation(naverMap: NaverMap) {

        // 마커 객체 생성 및 지도에 추가
        val marker = Marker()

        // 지도에 표시할 원 오버레이 객체
        val circle = CircleOverlay()



        userLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        userLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                var lat = 0.0
                var lng = 0.0
                if (location != null) {
                    lat = location.latitude
                    lng = location.longitude
                }

                Log.d(TAG,"현재 위치는 $lat , $lng 입니다.")

                // 선택 좌표 선언
                selected_Lat = lat
                selected_Lng = lng

                // 지도에 표시 //

                // 카메라 현재 위치로 이동
                val cameraUpdate = CameraUpdate.scrollTo(LatLng(lat, lng))
                naverMap.moveCamera(cameraUpdate)

                marker.position = LatLng( lat , lng )
                marker.map = naverMap
                circle.center = LatLng( lat , lng )
                circle.radius = 50.0    // 원 반경 50m
                circle.outlineWidth = 10
                circle.outlineColor = GREEN
                circle.color = 0
                circle.map = naverMap


            }
        }

        // 지도가 클릭되면 onMapClick() 콜백 메서드가 호출되며, 파라미터로 클릭된 지점의 화면 좌표와 지도 좌표가 전달됩니다.
        naverMap.setOnMapClickListener { pointF, coord ->
            // coord.latitude , coord.longitude -> 클릭을 통한 선택 지점의 좌표
            marker.position = LatLng( coord.latitude , coord.longitude )
            marker.map = naverMap
            circle.center = LatLng( coord.latitude , coord.longitude )
            circle.radius = 50.0    // 원 반경 50m
            circle.outlineWidth = 10
            circle.outlineColor = GREEN
            circle.color = 0
            circle.map = naverMap
            // 선택된 중심 좌표 값 저장
            selected_Lat = coord.latitude
            selected_Lng = coord.longitude
        }

    }



    override fun onStop() {
        super.onStop()
        userLocationListener = null
        userLocationManager = null
    }


}