package com.example.todowhere

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkManager
import com.example.todowhere.databinding.ActivityAddTodoBinding
import com.example.todowhere.databinding.ActivityMainBinding
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import org.jetbrains.anko.toast
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

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



    // real DB에서 사용할 id를 위한 현재 시간 변수
    var cur_time = Date().time
    var cur_time_form: String = SimpleDateFormat("HHmmss").format(cur_time)!! // 현재 시간을 원하는 형태로 변경

    // Location API를 사용하기 위한 geofencing client 인스턴스 생성
    private val geofencingClient : GeofencingClient by lazy {
        LocationServices.getGeofencingClient(this)
    }

    // BroadcastReceiver를 시작하는 PendingIntent 정의
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    // Geofencing을 저장할 리스트
    public val geofenceList : MutableList<Geofence> = mutableListOf()







    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        Log.d(TAG,"MainActivity 시작")

        // 권한을 확인하는 함수

        // 위치 권환 요청
        CheckPermission()

        selected_date = getDate(selected_year,selected_month,selected_day)
        var realmResult =
            realm.where<Todo>().contains("id",selected_date).findAll().sort("id",Sort.ASCENDING)

        // 해당 날짜에 추가된 일정 아무것도 없을 경우 빈 데이터 추가
        if (realmResult.size == 0 ) {
            add_blank_data(selected_date)
        }



        // 리사이클러뷰 관련 선언
        // MyAdapter를 생성 후 recyclerview의 adapter로 선언해줍니다.
        val myAdapter = MyAdapter(this,find_Item_Count(selected_date),realmResult)
        mainBinding.TodoRecyclerView.adapter = myAdapter


        // layout을 생성 후 recyclerview의 adapter로 선언해줍니다.
        val layout = LinearLayoutManager(this)
        mainBinding.TodoRecyclerView.layoutManager = layout



        // 캘린더뷰에서 날짜 선택 시 날짜 지정
        mainBinding.CalendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->

            // 날짜 선택 시 선택한 시간으로 갱신
            cur_time = Date().time
            cur_time_form = SimpleDateFormat("HHmmss").format(cur_time)

            // 날짜 선택시 선택한 날짜로 갱신
            selected_month = month + 1
            selected_year = year
            selected_day = dayOfMonth

            // 선택한 날짜를 yyyyMMdd 형태로 변형
            selected_date = getDate(selected_year,selected_month,selected_day)

            // 해당 날짜에 추가된 일정 아무것도 없을 경우 빈 데이터 추가
            realmResult =
                realm.where<Todo>().contains("id",selected_date).findAll().sort("id",Sort.ASCENDING)
            Log.d(TAG,"$realmResult")

            if (realmResult.size == 0 ) {
                add_blank_data(selected_date)
            }

            myAdapter.todo_datas = realmResult


            // 선언한 adapter 객체의 Item으로 캘린더에서 선택한 날짜의 아이템 수를 다시 입력
            myAdapter.Item = find_Item_Count(selected_date)

            // adapter에게 Data가 변했다는 것을 알려줍니다.
            myAdapter.notifyDataSetChanged()


            Log.d(TAG, "선택한 날짜는 $year - ${month + 1} - $dayOfMonth 입니다.")
            Log.d(TAG, "선택했을때 시간은 $cur_time_form 입니다.")
        }




        // 날짜 선택 후 일정 추가 버튼 클릭 시 yyyyMMdd 형태로 전달
        myAdapter.setonBtnClickListener(object : MyAdapter.onBtnClickListener {

            // onBtnClick 오버라이드 정의
            override fun onBtnClick() {

                Log.d(TAG,"일정 추가 버튼 클릭 !!")

                var next_intent = Intent(this@MainActivity, AddTodoActivity::class.java).apply {
                    // 선택한 날짜 넘겨주기
                    Log.d(TAG,"선택한 날짜 : $selected_date")
                    Log.d(TAG,"선택한 시간 : $cur_time_form")

                    putExtra("DATE", selected_date)
                    putExtra("TIME", cur_time_form)
                    putExtra("GeofenceList",geofenceList)

                }
                startActivity(next_intent)
            }
        })



    }

    // AddTodoActivity에서 일정 추가 후 onResum호출을 통해 바로 일정 추가
    override fun onResume() {
        super.onResume()
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

    }

    // 날짜를 원하는 8자리로 만들어주는 함수
   fun getDate(year : Int , month : Int , day : Int) : String {

        var date : String

        if (month < 10) {
            date =
                year.toString() + '0' + month.toString() + day.toString()

        } else {
            date =
                year.toString() + month.toString() + day.toString()
        }

        return date

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

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
//                if (grantResults.isEmpty()) {
//                    Log.d(TAG,"권한 결과가 비었음1")
//                    throw RuntimeException("Empty permission result")
//                }

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
                        // 세팅으로 가서 무조건 허용하도록 작성성
                    }
                }
            }
        }
    }

    // 무조건 권한을 받아오는 함수
    private fun GetPermission() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("권한 필수 요청")
            .setMessage("위치 서비스를 이용하기 위해 권한이 반드시 허용되어야 합니다.")

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
            .setCircularRegion(geo.first,geo.second,radius)    // 위치 및 반경(m)
            .setExpirationDuration(time)    // Geofence 만료 시간 ,단위 : milliseconds
            .setLoiteringDelay(10000)    // 지오펜싱 입장과 머물기를 판단하는데 필요한 시간, 단위 : milliseconds
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT or Geofence.GEOFENCE_TRANSITION_DWELL)
            .build()
    }

    // Geofence 지정 및 관련 이벤트 트리거 방식을 설정하기 위해 GeofencingRequest 빌드
    // INITIAL_TRIGGER_ENTER를 지정하면 기기가 이미 지오펜싱 내부에 있는 경우 GEOFENCE_TRANSITION_ENTER를
    // 트리거해야 한다고 위치 서비스에 알림
    private fun getGeofencingRequest(list:List<Geofence>):GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            // Geofence 이벤트는 진입시부터 처리할 때
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(list)  // Geofence 리스트 추가
        }.build()
    }

    @SuppressLint("MissingPermission")
    private fun addGeofences() {
        CheckPermission()
        geofencingClient.addGeofences(getGeofencingRequest(geofenceList),geofencePendingIntent).run {
            addOnSuccessListener {
                Toast.makeText(this@MainActivity,"add Success", Toast.LENGTH_LONG).show()
            }
            addOnFailureListener {
                Toast.makeText(this@MainActivity, "add Fail", Toast.LENGTH_LONG).show()
            }
        }
    }

}

