package com.example.todowhere.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
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
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.todowhere.*
import com.example.todowhere.CalendarDecorator.TodayDecorator
import com.example.todowhere.RealmDB.Geofencing
import com.example.todowhere.RealmDB.Todo
import com.example.todowhere.databinding.ActivityMainBinding
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer

/*
<수정 요청> 2022 - 02 - 06

앱 아이콘 변경 - 해결

 */


class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding

    private var doubleBackToExit = false    // 메인액티비티에서 종료시 판단하는 변수

    var mLocationManager: LocationManager? = null    // 위치 서비스에 접근하는 클래스를 제공
    var mLocationListener: LocationListener? = null    // 위치가 변할 때 LocationManager로부터 notification을 받는 용도

    private lateinit var myAdapter: MyAdapter    // 리사이클러뷰 어댑터
    private lateinit var layout: LinearLayoutManager    // 리사이클러뷰 레이아웃

    private var timerTask : Timer? = null

    val todayGeofenceList : MutableList<Geofence> = mutableListOf()    // 오늘 작동할 Geofencing만 담은 리스트


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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        Log.d(TAG,"onCreate() 시작")

        CheckPermission()    // 위치 권환 요청

        whenUpdateLocation()    // 위치가 업데이트될 때
        getTodayGeofencing()    // 지오펜싱 DB에서 오늘 날짜의 지오펜싱들 추가
        startTimer()    // 타이머 진행
        resetAt3AM()    // 매일 초기화 알람 실행
        initAdapter()    // 어댑터에 관한 설정
        initCalendarView()    // 캘린더 뷰에 관한 설정


    }




    // AddTodoActivity에서 일정 추가 후 onResum호출을 통해 바로 일정 추가 And Geofencing 추가
    override fun onResume() {
        super.onResume()

        Log.d(TAG,"onResume() 시작")
        initCalendarView()    // 앱 재실행시 캘린더뷰 설정
        initAdapter()    // 다시 재개시 어댑터 업데이트


        /////// Geofencing 추가 코드 //////
        // geofenceList에 새로 입력받은 값 추가
        // 새로운 좌표를 입력 받은 경우만 추가

        getTodayGeofencing()
        if (todayGeofenceList.isNotEmpty()) {
            addGeofences()    // geofencing 추가
        }

        // adapter에게 Data가 변했다는 것을 알려줍니다.
        myAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()   // 인스턴스 해제
    }

    // 뒤로가기 클릭 시
    override fun onBackPressed() {
        // 두번 뒤로 가기 클릭으로 종료 명령 시 종료
        if (doubleBackToExit) {
            finishAffinity()
        } else {
            Toast.makeText(this, "종료하시려면 뒤로가기를 한번 더 눌러주세요.", Toast.LENGTH_SHORT).show()
            // 1.5초 사이에 뒤로 가기를 한번 더 누를 경우 위의 종료 명령 실행
            doubleBackToExit = true
            runDelayed(1500L) {
                doubleBackToExit = false
            }
        }
    }


    fun runDelayed(millis: Long, function: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed(function, millis)
    }

    // 캘린더뷰에 관한 설정
    private fun initCalendarView() {

        // 데코레이터들 선언
        val todayDecorator = TodayDecorator(this)    // 오늘 날짜에 표시

        selected_date = getDate(selected_year,selected_month,selected_day)


        mainBinding.CalendarView.setCurrentDate(Date(System.currentTimeMillis()))    // 오늘 날짜로 설정
        mainBinding.CalendarView.setDateSelected(Date(System.currentTimeMillis()),true)    // 오늘 날짜 선택

        // 데코레이터들 추가
        mainBinding.CalendarView.addDecorators(todayDecorator)

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
            var realmResult = realm.where<Todo>().contains("id",selected_date).findAll().sort("id",Sort.ASCENDING)

            if (realmResult.size == 0 ) {
                add_blank_data(selected_date)
            }

            myAdapter.todo_datas = realmResult

            // 선언한 adapter 객체의 Item으로 캘린더에서 선택한 날짜의 아이템 수를 다시 입력
            myAdapter.Item = find_Item_Count(selected_date)

            // adapter에게 Data가 변했다는 것을 알려줍니다.
            myAdapter.notifyDataSetChanged()

//            Log.d(TAG, "선택한 날짜는 $selected_year - ${selected_month} - $selected_day 입니다.")
//            Log.d(TAG, "선택했을때 시간은 $cur_time_form 입니다.")
//            Log.d(TAG, "DB : $realmResult")

            Log.d(TAG,"오늘의 지오펜싱 리스트 : $todayGeofenceList")

        }

    }


    // 어댑터의 클릭 리스너 설정
    private fun initAdapter() {

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

        myAdapter.todo_datas = realmResult

        // 선언한 adapter 객체의 Item으로 캘린더에서 선택한 날짜의 아이템 수를 다시 입력
        myAdapter.Item = find_Item_Count(selected_date)



        // 날짜 선택 후 일정 추가 버튼 클릭 시 yyyyMMdd 형태로 전달
        myAdapter.setOnAddBtnClickListener(object : MyAdapter.OnAddBtnClickListener {

            // onBtnClick 오버라이드 정의
            override fun onAddClick() {

                // 오늘보다 이전에는 일정을 추가할 수 없다.
                if (selected_date.toInt() < today_date.toInt()) {
                    Toast.makeText(this@MainActivity,"과거에는 일정을 추가하실 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@onAddClick
                }

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
                        // DB에서 삭제 및 지오펜싱 삭제
                        deleteFromDB(todo.id)
                        // TODO 지오펜싱 리스트에서 지오펜스 또한 삭제해야한다.
                        deleteGeofencing(todo)

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
            override fun onMapClick(todo: Todo) {
                Log.d(TAG,"맵 버튼 클릭")
                // TODO 위치를 띄우는 팝업 구현
                val dialog = MapDialog(todo)
                dialog.show(supportFragmentManager, "MapDialog")

            }
        })
    }


    private fun startTimer() {
        timerTask?.cancel()    // null이 아닐 경우 취소하고 실행
        timerTask = timer(period = 1000) {
            runOnUiThread {
                // 진행할 내용
                realm.beginTransaction()

                // 매초 "Doing"인 상태의 할 일을 카운트    equalTo("id",today_date).
                var realmResult = realm.where<Todo>().equalTo("state","Doing").contains("id",today_date).findAll()
                // Log.d(TAG,"진행 중인 realm : $realmResult")
                realmResult.forEach {
                    var nowTime = it.time
                    nowTime -= 1
                    it.time = nowTime

                    // 0초가 되면 종료
                    if (nowTime <= 0L && it.state != "Done") {
                        it.state = "Done"
                    }
                }

                realm.commitTransaction()

                myAdapter.notifyDataSetChanged()    // 어댑터에 적용
            }
        }
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

        return realmResult.size

    }

    // 일정 추가를 위한 빈 데이터 추가
    fun add_blank_data(date : String) {
        realm.beginTransaction()
        // id를 선택한 날짜 + 999999 형태로 설정
        val blank_item = realm.createObject<Todo>(date + "999999")
        blank_item.what = "BLANK"

        realm.commitTransaction()
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

        geofencingClient.addGeofences(getGeofencingRequest(todayGeofenceList),geofencePendingIntent).run {
            addOnSuccessListener {
                Toast.makeText(this@MainActivity,"add Success", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "추가 후 지오펜싱 리스트 : ${todayGeofenceList}")
            }
            addOnFailureListener {
                Toast.makeText(this@MainActivity, "add Fail", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 지오펜싱 DB에서 오늘 날짜의 지오펜싱들을 리스트에 추가하는 함수
    private fun getTodayGeofencing() {

        val realmResult = realm.where<Geofencing>().contains("id",today_date).findAll()

        realmResult.forEach {
            var nowGeofence = getGeofence(it.id,(Pair(it.lat,it.lng)),50f,Geofence.NEVER_EXPIRE)
            if (todayGeofenceList.contains(nowGeofence)) {
            } else {
                todayGeofenceList.add(nowGeofence)
            }
        }

        Log.d(TAG," 현재 지오펜싱 리스트 : ${todayGeofenceList}")

    }

    private fun deleteGeofencing(todo: Todo) {

        val realmResult = realm.where<Geofencing>().equalTo("id",todo.id).findFirst()!!

        var nowGeofence = getGeofence(realmResult.id,(Pair(realmResult.lat,realmResult.lng)),50f,Geofence.NEVER_EXPIRE)
        todayGeofenceList.remove(nowGeofence)


    }


    // TODO 22.03.28 - AlarmManager로 실행시키도록 변경) 오늘 날짜의 지오펜싱들을 전체 지오펜싱 리스트에서 찾아서 추가
    // 매일 상태를 리셋할 함수
    // 함수를 통해 ResetWorker를 호출하는 요청을 보내고 , ResetWorker안에서 자기 자신을 다시 호출하는
    // OneTimeWorkRequestBuilder를 요청하여 매일 반복하도록 한다.

    // 3시에 리셋시키는 알람매니저 등록
    private fun resetAt3AM() {

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager    // 알람매니져 객체 생성
        // BroadcastReceiver를 불러올 인텐트
        val intent = Intent(this,ResetBroadcastReceiver::class.java).apply {
            putExtra("yesterdayDate",today_date)
        }
        // 선언한 인테트를 불러올 Pending Intent , FLAG_UPDATE_CURRENT : 현재 PendingIntent를 유지하고, 대신 인텐트의 extra data는 새로 전달된 Intent로 교체
        val resetPendingIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // 3:00 AM에 초기화
        val resetCalendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 3)
        }

        // 정시에 실행하고 반복하는 명령
        // Inexcat -> 정확하지 않다 , 즉 정확하진 않지만 반복적으로 알람이 제공되는 명령
        // 해당 메소드는 OS가 잠자기 모드에 들어가도 실행되지않고 정확하지 않기 때문에 현 예제에서 참고용으로만 쓰인다.
        // alarmManager.setAndAllowWhileIdle()  *잠자기 모드에서도 정확한 alarmManager 조건을 탐지하는 메소드
        // alarmManager.setExactAndAllowWhileIdle()
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            resetCalendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            resetPendingIntent
        )
    }


    fun deleteFromDB(ID: String) {

        realm.beginTransaction()

        // 일정 DB에서 삭제
        var todoResult = realm.where<Todo>().equalTo("id",ID).findFirst()
        if (todoResult != null) {
            Log.d(ContentValues.TAG, "삭제 일정 : $todoResult")
            todoResult.deleteFromRealm()
        }

        // 지오펜싱 삭제
        var geofencingResult = realm.where<Geofencing>().equalTo("id",ID).findFirst()
        if (geofencingResult != null) {
            Log.d(ContentValues.TAG, "삭제 지오펜싱 : $geofencingResult")
            geofencingResult.deleteFromRealm()

        }

        realm.commitTransaction()

    }



    // 현재 위치
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



    /////////////// 권한 관련 함수  /////////////////////
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
                        Log.d(TAG,"FINE_LOCATION 사용자가 권한 거부")
                        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),FINE_LOCATION_REQUEST_CODE)
                    } else {
                        // 권한을 처음 보거나 다시 묻지 않음을 선택하거나 권한을 허용한 경우 false 리턴
                        Log.d(TAG,"FINE_LOCATION 사용자가 권한 허용")
                        // 세팅으로 가서 무조건 허용하도록 작성

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
                        Log.d(TAG,"BACKGROUND 사용자가 권한 거부")
                        // requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),FINE_BACKGROUND_REQUEST_CODE)
                        GetPermission()
                    } else {
                        // 권한을 처음 보거나 다시 묻지 않음을 선택하거나 권한을 허용한 경우 false 리턴
                        Log.d(TAG,"BACKGROUND 사용자가 권한 허용")
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
            Toast.makeText(this, "앱을 사용하기 위해선 백그라운드 위치 권한이 필요합니다." , Toast.LENGTH_SHORT).show()
            finishAffinity()    // 앱 종료

        }
        val dialog = builder.create()
        dialog.show()

    }

    companion object {
        private const val ALARM_REQUEST_CODE = 1000
    }



}

