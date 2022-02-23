package com.example.todowhere.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.todowhere.*
import com.example.todowhere.data.Todo
import com.example.todowhere.databinding.ActivityMainBinding
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.naver.maps.map.MapFragment
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/*
<수정 요청> 2022 - 02 - 06
할일추가시 현재 위치로 설정되어 있지 않음 - 해결

지오 펜싱 작동 유무 재확인 + 지오펜싱 작동 시 UI에서 알려주는 객체 필요 - waveView로 표시
-> 지오 펜싱 작동 시 상호 작용 변경중 2022 - 02 - 10 , Geofencing이 실행되지 않는다.

ResetWorker 다시 코딩

일정 추가 후 앱 상태 Stop으로 자동 복구 - 필요 없는 기능 삭제 2022 - 02 - 10

앱 아이콘 변경

 */


class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding

    var mLocationManager: LocationManager? = null    // 위치 서비스에 접근하는 클래스를 제공
    var mLocationListener: LocationListener? = null    // 위치가 변할 때 LocationManager로부터 notification을 받는 용도

    private lateinit var myAdapter: MyAdapter
    private lateinit var layout: LinearLayoutManager

    private lateinit var timerTask: Timer

    // Geofencing을 저장할 리스트
    val geofenceList : MutableList<Geofence> = mutableListOf()

    // Location API를 사용하기 위한 geofencing client 인스턴스 생성
    private val geofencingClient : GeofencingClient by lazy { // 지오펜싱 클라이언트의 인스턴스
        LocationServices.getGeofencingClient(this)
    }

    // BroadcastReceiver를 시작하는 PendingIntent 정의
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    // 오늘 날짜로 캘린더 객체 생성
    val calendar: Calendar = Calendar.getInstance()
    var TAG: String = "로그"
    val FINE_LOCATION_REQUEST_CODE = 100
    val FINE_BACKGROUND_REQUEST_CODE = 200

    // realm 사용을 위한 객체 선언
    val realm = Realm.getDefaultInstance()

    // 년도, 월, 일 변수를 선언
    var selected_year = calendar.get(Calendar.YEAR)
    var selected_month = calendar.get(Calendar.MONTH) + 1
    var selected_day = calendar.get(Calendar.DAY_OF_MONTH)
    var selected_date: String =
        selected_year.toString() + selected_month.toString() + selected_day.toString()
    var today_date = getDate(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.DAY_OF_MONTH))    // 오늘 날짜 8자리로 표현

    // real DB에서 사용할 id를 위한 현재 시간 변수
    var cur_time = Date().time
    var cur_time_form: String = SimpleDateFormat("HHmmss").format(cur_time)!! // 현재 시간을 원하는 형태로 변경


    // Geofencing 객체를 만들기 위해 AddTodoActivity에서 불러온 좌표값 , 목표 달성 시간
    var saved_Lat : Double = 0.0
    var saved_Lng : Double = 0.0
    var saved_time : Int = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        Log.d(TAG,"onCreate() 시작")

        // 위치 권환 요청
        CheckPermission()

        // 매일 새벽 3시 모든 상태 초기화 및 지오펜싱 삭제
        resetworkManager()

        // 위치가 업데이트될 때
        whenUpdateLocation()

        // AddTodoActivity 에서 인텐트를 전달받기 위해 선언
        var intent_from_addtodoactivity = getIntent()

        // AddTodoActivity 에서 선택한 좌표값들 선언언
        saved_Lat = intent_from_addtodoactivity.getDoubleExtra("Lat",0.0)
        saved_Lng = intent_from_addtodoactivity.getDoubleExtra("Lng",0.0)


        selected_date = getDate(selected_year,selected_month,selected_day)


        var realmResult = realm.where<Todo>().contains("id",selected_date).findAll().sort("id",Sort.ASCENDING)


        // 해당 날짜에 추가된 일정 아무것도 없을 경우 빈 데이터 추가
        if (realmResult.size == 0 ) {
            add_blank_data(selected_date)
        }




        // 리사이클러뷰 관련 선언
        // MyAdapter를 생성 후 recyclerview의 adapter로 선언해줍니다.
        myAdapter = MyAdapter(this,find_Item_Count(selected_date),realmResult)
        mainBinding.TodoRecyclerView.adapter = myAdapter


        // layout을 생성 후 recyclerview의 adapter로 선언해줍니다.
        layout = LinearLayoutManager(this)
        mainBinding.TodoRecyclerView.layoutManager = layout



        // runOnUIThread로 아이템 시간 변화주기
        ChangeTimeThread()

        // 날짜 선택 후 일정 추가 버튼 클릭 시 yyyyMMdd 형태로 전달
        myAdapter.setOnAddBtnClickListener(object : MyAdapter.OnAddBtnClickListener {

            // onBtnClick 오버라이드 정의
            override fun onAddClick() {

                Log.d(TAG,"일정 추가 버튼 클릭 !!")

                var next_intent = Intent(this@MainActivity, AddTodoActivity::class.java).apply {
                    // 선택한 날짜 넘겨주기
                    Log.d(TAG,"선택한 날짜 : $selected_date")
                    Log.d(TAG,"선택한 시간 : $cur_time_form")

                    putExtra("DATE", selected_date)
                    putExtra("TIME", cur_time_form)
                }
                startActivity(next_intent)
            }
        })


        // 리사이클러뷰 아이템 삭제 버튼 클릭 시
        myAdapter.setOnDelBtnClickListener(object : MyAdapter.OnDelBtnClickListener {

            override fun onDelClick(todo: Todo) {

                Log.d(TAG , "일정 삭제 버튼 클릭 !!")

                // Todo 1. 삭제할건지 다시 묻는 Dialog   2. 삭제 시 DB에서 데이터 삭제 및 아이템 개수 -1
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("일정 삭제")
                    .setMessage("해당 일정을 정말로 삭제하시겠습니까?")
                    .setPositiveButton("삭제",{ _,_ ->
                        // Todo DB에서 해당 일정 삭제
                        deleteFromDB(todo.id)

                        // Adapter에 변화된 realm 값들을 적용시켜줘야한다.
                        myAdapter.todo_datas =
                            realm.where<Todo>().contains("id",selected_date).findAll().sort("id",Sort.ASCENDING)
                        myAdapter.Item = find_Item_Count(selected_date)

                        // 단순하게 Adapter의 데이터가 변화했다고 알려주는 메서드
                        // 변화한 데이터는 직접 다시 갱신시켜줘야 한다.
                        myAdapter.notifyDataSetChanged()

                    })
                    .setNegativeButton("취소" ,{ _,_ ->
                        // 팝업 닫기
                    }).show()
            }

        })

        // Map버튼 클릭 시
        myAdapter.setOnMapBtnClickListener(object : MyAdapter.OnMapBtnClickListener {
            override fun onMapClick(todo:Todo) {
                Log.d(TAG,"맵 버튼 클릭")
                // TODO 위치를 띄우는 팝업 구현
                val dialog = MapDialog(todo)
                dialog.show(supportFragmentManager, "MapDialog")
//                val mapDialogView = LayoutInflater.from(this@MainActivity).inflate(R.layout.map_popup,null)
//                val mapBuilder = AlertDialog.Builder(this@MainActivity)
//                    .setView(mapDialogView)
//                    // .create()
//
//                val mAlertDialog = mapBuilder.show()
//
//                val backBtn = mapDialogView.findViewById<ImageButton>(R.id.backBtn)
//                backBtn.setOnClickListener {
//                    mAlertDialog.dismiss()
//                }

            }
        })

        mainBinding.CalendarView.setCurrentDate(Date(System.currentTimeMillis()))    // 오늘 날짜로 설정
        mainBinding.CalendarView.setDateSelected(Date(System.currentTimeMillis()),true)    // 오늘 날짜 선택

        // 캘린더뷰에서 날짜 선택 시 날짜 지정
        mainBinding.CalendarView.setOnDateChangedListener { widget, date, selected ->   //{ view, year, month, dayOfMonth ->

            // 날짜 선택 시 선택한 시간으로 갱신
            cur_time = Date().time
            cur_time_form = SimpleDateFormat("HHmmss").format(cur_time)

            // 날짜 선택시 선택한 날짜로 갱신
            selected_month = date.month + 1
            selected_year = date.year
            selected_day = date.day

            // 선택한 날짜를 yyyyMMdd 형태로 변형
            selected_date = getDate(selected_year,selected_month,selected_day)

            // 해당 날짜에 추가된 일정 아무것도 없을 경우 빈 데이터 추가
            realmResult = realm.where<Todo>().contains("id",selected_date).findAll().sort("id",Sort.ASCENDING)
            Log.d(TAG,"$realmResult")

            if (realmResult.size == 0 ) {
                add_blank_data(selected_date)
            }

            myAdapter.todo_datas = realmResult

            // 선언한 adapter 객체의 Item으로 캘린더에서 선택한 날짜의 아이템 수를 다시 입력
            myAdapter.Item = find_Item_Count(selected_date)

            // adapter에게 Data가 변했다는 것을 알려줍니다.
            myAdapter.notifyDataSetChanged()

            Log.d(TAG, "선택한 날짜는 $selected_year - ${selected_month} - $selected_day 입니다.")
            Log.d(TAG, "선택했을때 시간은 $cur_time_form 입니다.")
            Log.d(TAG, "DB : $realmResult")
        }


    }

    // 0.5초마다 어댑터에 변화 감지
    private fun ChangeTimeThread() {

            // 0.5초마다 반복
            timerTask = kotlin.concurrent.timer(period = 500) {
                // UI조작을 위한 메서드
                runOnUiThread {
                    myAdapter.notifyDataSetChanged()
                    // Log.d(TAG,"어댑터에 알리는 중입니당")
                }
            }

    }


    // AddTodoActivity에서 일정 추가 후 onResum호출을 통해 바로 일정 추가 And Geofencing 추가
    override fun onResume() {
        super.onResume()

        Log.d(TAG,"onResume() 시작")
        // 이거 없으면 클릭리스너 작동 X - 11.18
        val mainBinding = ActivityMainBinding.inflate(layoutInflater)

        var realmResult =
            realm.where<Todo>().contains("id",selected_date).findAll().sort("id",Sort.ASCENDING)

        // 해당 날짜에 추가된 일정 아무것도 없을 경우 빈 데이터 추가
        if (realmResult.size == 0 ) {
            add_blank_data(selected_date)
        }

        val myAdapter = MyAdapter(this,find_Item_Count(selected_date),realmResult)
        mainBinding.TodoRecyclerView.adapter = myAdapter


        // layout을 생성 후 recyclerview의 adapter로 선언해줍니다.
        val layout = LinearLayoutManager(this)
        mainBinding.TodoRecyclerView.layoutManager = layout
        myAdapter.todo_datas = realmResult

        // 선언한 adapter 객체의 Item으로 캘린더에서 선택한 날짜의 아이템 수를 다시 입력
        myAdapter.Item = find_Item_Count(selected_date)

        // adapter에게 Data가 변했다는 것을 알려줍니다.
        myAdapter.notifyDataSetChanged()



        /////// Geofencing 추가 코드 //////
        // geofenceList에 새로 입력받은 값 추가
        // ID는 realm DB에 들어가는 id와 동일하게 적용
        geofenceList.add(getGeofence(selected_date+cur_time_form,(Pair(saved_Lat,saved_Lng)),50f,saved_time.toLong()))
        addGeofences()    // geofencing 추가

    }


    // 날짜를 원하는 8자리로 만들어주는 함수
   fun getDate(year : Int , month : Int , day : Int) : String {

        var month_str : String
        var day_str : String

        if (month < 10) {
            month_str = '0' + month.toString()
        } else {
            month_str = month.toString()
        }

        if (day < 10) {
            day_str = '0' + day.toString()
        } else {
            day_str = day.toString()
        }

        return year.toString() + month_str + day_str

    }

    // mainActivity에서 선언된 날짜에 해당하는 id를 가진 값들 반환
    fun find_Item_Count(date : String) : Int {

        val realmResult = realm.where<Todo>().contains("id",date).findAll()

        Log.d(TAG," 지금 아이템 수 : ${realmResult.size}")

        return realmResult.size

    }

    // 일정 추가를 위한 빈 데이터 추가
    fun add_blank_data(date : String) : Unit {
        realm.beginTransaction()
        // id를 선택한 날짜 + 999999 형태로 설정
        val blank_item = realm.createObject<Todo>(date + "999999")
        blank_item.what = "BLANK"

        realm.commitTransaction()

    }

    // 권한 확인
    fun CheckPermission() {

        // ACCESS_FINE_LOCATION 허용 시 ACCESS_COARSE_LOCATION 권한도 허용, 반대는 X
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
            // 권한이 거부했을 경우
            Log.d(TAG,"FINE_LOCATION 접근 권한 불허")
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),FINE_LOCATION_REQUEST_CODE)
            // 위의 권한 요청에 따른 결과들은 onRequestPermissionsResult 함수로 출력

        } else {
            // 권한이 허용된 경우
            Log.d(TAG,"FINE_LOCATION 접근 권한 허용")
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED) {
            // 권환을 거부했을 경우
            Log.d(TAG,"BACKGROUND_LOCATION 접근 권한 불허")
            requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),FINE_BACKGROUND_REQUEST_CODE)
            // 위의 권한 요청에 따른 결과들은 onRequestPermissionsResult 함수로 출력

        } else {
            // 권한이 허용된 경우
            Log.d(TAG,"BACKGROUND_LOCATION 접근 권한 허용")
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            FINE_LOCATION_REQUEST_CODE -> {
//                if (grantResults.isEmpty()) {
//                    Log.d(TAG,"권한 결과가 비었음")
//                    // throw RuntimeException("Empty permission result")
//                }

                if (grantResults.size != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한이 허용된 경우
                    Log.d(TAG,"FINE_LOCATION 권한 허용")
                } else {
                    // 권한이 명시적으로 거부된 경우( Deny 버튼 )
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Log.d(TAG,"사용자가 권한 거부")
                        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),FINE_LOCATION_REQUEST_CODE)
                    } else {
                        // 권한을 처음 보거나 다시 묻지 않음을 선택하거나 권한을 허용한 경우 false 리턴
                        Log.d(TAG,"사용자가 권한 허용")
                        // 세팅으로 가서 무조건 허용하도록 작성성

                   }
                }
            }

            FINE_BACKGROUND_REQUEST_CODE -> {

                if (grantResults.size != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한이 허용된 경우
                    Log.d(TAG,"BACKGROUND 권한 허용")
                } else {
                    // 권한이 거부된 경우( Deny 버튼 )
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        Log.d(TAG,"사용자가 권한 거부")
                        // requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),FINE_BACKGROUND_REQUEST_CODE)
                        GetPermission()
                    } else {
                        // 권한을 처음 보거나 다시 묻지 않음을 선택하거나 권한을 허용한 경우 false 리턴
                        Log.d(TAG,"사용자가 권한 허용")
                        // 세팅으로 가서 무조건 허용하도록 작성
                    }
                }
            }
        }
    }

    // 무조건 권한을 받아오는 함수
    private fun GetPermission() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("권한 필수 요청")
            .setMessage("백그라운드 위치 서비스를 이용하기 위해 권한이 반드시 허용되어야 합니다.")

        builder.setPositiveButton("OK") { dialogInterface, i ->
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)   // 6
        }

        builder.setNegativeButton("Later") { dialogInterface, i ->
            // ignore
        }
        val dialog = builder.create()
        dialog.show()

    }

    // 지오펜스 객체를 생성하는 함수
    private fun getGeofence(reqId:String , geo:Pair<Double,Double>, radius:Float = 50f, time:Long):Geofence {
        return Geofence.Builder()
            .setRequestId(reqId)    // 이벤트 발생시 BroadcastReceiver에서 구분할 id
            .setCircularRegion(geo.first, geo.second, radius)    // 위치 및 반경(m)
            .setExpirationDuration(time)    // Geofence 만료 시간 ,단위 : milliseconds
            .setLoiteringDelay(1000)    // 지오펜싱 입장과 머물기를 판단하는데 필요한 시간, 단위 : milliseconds TODO DWELL 판단 시간 나중에 수정 필요
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or
                        Geofence.GEOFENCE_TRANSITION_EXIT or
                        Geofence.GEOFENCE_TRANSITION_DWELL)
            .build()
    }

    // Geofence 지정 및 관련 이벤트 트리거 방식을 설정하기 위해 GeofencingRequest 빌드
    // INITIAL_TRIGGER_ENTER를 지정하면 기기가 이미 지오펜싱 내부에 있는 경우 GEOFENCE_TRANSITION_ENTER를
    // 트리거해야 한다고 위치 서비스에 알림
    private fun getGeofencingRequest(list:List<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            // Geofence 이벤트는 진입시부터 처리할 때
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or
                    GeofencingRequest.INITIAL_TRIGGER_DWELL or
                    GeofencingRequest.INITIAL_TRIGGER_EXIT)
            addGeofences(list)  // Geofence 리스트 추가
        }.build()
    }

    @SuppressLint("MissingPermission")
    private fun addGeofences() {
        CheckPermission()
        geofencingClient.addGeofences(getGeofencingRequest(geofenceList),geofencePendingIntent).run {
            addOnSuccessListener {
                Toast.makeText(this@MainActivity,"add Success", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "지오펜싱 리스트 : ${geofenceList}")
            }
            addOnFailureListener {
                Toast.makeText(this@MainActivity, "add Fail", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 매일 상태를 리셋할 함수
    private fun resetworkManager() {
        val dailyResetRequeset = OneTimeWorkRequestBuilder<ResetWorker>()
                .setInitialDelay(getTimeUsingInWorkRequest(), TimeUnit.MILLISECONDS)    // 초기 지연 설정
            .addTag("Reset")
            .build()


        WorkManager.getInstance(this).enqueue(dailyResetRequeset)
    }

    // 실행 지연 시간을 설정하는 함수
    fun getTimeUsingInWorkRequest() : Long {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()

        dueDate.set(Calendar.HOUR_OF_DAY, 3)
        dueDate.set(Calendar.MINUTE, 0)
        dueDate.set(Calendar.SECOND, 0)

        if(dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        return dueDate.timeInMillis - currentDate.timeInMillis
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

    private fun whenUpdateLocation() {
        mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        mLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                var lat = 0.0
                var lng = 0.0
                if (location != null) {
                    lat = location.latitude
                    lng = location.longitude
                    Log.d("로그", " 현재 위치는 $lat , $lng ")
                }

                Toast.makeText(this@MainActivity,"현재 위치는 $lat , $lng 입니다.",Toast.LENGTH_SHORT).show()


            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        mLocationManager!!.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            3000L,
            30f, mLocationListener as LocationListener
        )
    }




    override fun onDestroy() {
        super.onDestroy()
        realm.close()   // 인스턴스 해제
    }


//    private fun getTodayTodo(Today_Date:String):MutableList<Todo> {
//
//        realm.beginTransaction()    // realm 트랜잭션 시작
//
//        val realmResult = realm.where(Todo::class.java).contains("id",Today_Date).findAll()
//        return realmResult.subList(0,realmResult.size)
//
//        // slice 와 subList의 차이점
//        // 둘 다 시작인덱스와 목표 인덱스를 지정하여 원하는 부분을 가져온다
//        // slice는 원본 리스트의 값들을 복사해서 들고 온다, subList는 원본 리스트를 참고하여 가져오기 때문에 원본 리스트에서
//        // 원소의 변화가 있을 경우 slice는 변화에 대응하지 않고 이전에 복사한 값 그대로이고 subList는 변경된 인덱스를 그대로 참조합니다.
//        /*
//            fun main() {
//        val myList = mutableListOf(1, 2, 3, 4)
//        val subList = myList.subList(1, 3)
//        val sliceList = myList.slice(1..2)
//        println(subList) // [2, 3]
//        println(sliceList) // [2, 3]
//        myList[1] = 5
//        println(subList) // [5, 3]
//        println(sliceList) // [2, 3]
//        }
//         */
//    }

}

