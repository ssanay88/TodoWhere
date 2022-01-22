package com.example.todowhere

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.todowhere.DTO.GetAllDto
import com.example.todowhere.data.Todo

import com.example.todowhere.databinding.MapPopupBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapDialog(val todo: Todo) : DialogFragment(), OnMapReadyCallback {


    private lateinit var mapView: MapView
    private lateinit var reverseGeocodingService: ReverseGeocodingService    // reverse geocoding 서비스

    private var binding:MapPopupBinding? = null
    private val MapBinding get() = binding!!
    private val TAG = "태그"

    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {

        binding = MapPopupBinding.inflate(inflater,container,false)
        val view = MapBinding.root

        // MapFragment를 다른 프래그먼트 내에 배치할 경우 childFragmentManager를 사용해 둔다
        val fm = childFragmentManager
        val mapFragment = fm.findFragmentById(R.id.MapPopUpView) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.MapPopUpView, it).commit()
            }

        mapFragment.getMapAsync(this)


        // retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://naveropenapi.apigw.ntruss.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // reverseGeocoding 서비스 생성
        reverseGeocodingService = retrofit.create(ReverseGeocodingService::class.java)

        // 지도에 마커 표시


        // 주소 표시
        getAddress(todo)

        // 취소 버튼 클릭 시
        MapBinding.backBtn.setOnClickListener {
            dismiss()
        }



        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.MapPopUpView)
        mapView.onCreate(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        mapView.onDestroy()
    }


    private fun getAddress(todo: Todo) {

        val lat = todo.center_lat.toString()
        val lng = todo.center_lng.toString()



        reverseGeocodingService.getGeocoding(
            BuildConfig.REVESEGEOCODING_API_KEY_ID,
            BuildConfig.REVESEGEOCODING_API_KEY,
            "$lat,$lng")
            .enqueue(object : Callback<GetAllDto> {

                override fun onResponse(
                    call: Call<GetAllDto>,
                    response: Response<GetAllDto>
                ) {
                    if (response.isSuccessful.not()) {
                        Log.e(TAG,"여기서 실패")
                        Log.e(TAG,"응답에 실패했습니다.")
                        return
                    }

                    response.body().let {
                        // TODO 응답은 했지만 NULL 출력
                        Log.d(TAG,"${it?.status?.code} , ${it?.status?.name}")
                        it?.results?.forEach {
                            Log.d(TAG,it.name)
                            Log.d(TAG,"${it.region.area1.name} , ${it.region.area2.name} , ${it.land.name} , ${it.land.number1} ,${it.land.addition0.value}")

                            MapBinding.addressTextView.text =
                                it.region.area1.name + it.region.area2.name + it.land.name + it.land.number1 + it.land.addition0.value

                        }

//                                Log.d(TAG,"${it?.result?.results.region?.area1?.name} , ${it?.result?.region?.area2?.name} ,${it?.result?.region?.area3?.name}")
                    }

                }

                override fun onFailure(call: Call<GetAllDto>, t: Throwable) {
                    Log.e(TAG,t.toString())
                }

            })


    }

    override fun onMapReady(naverMap: NaverMap) {

        val marker = Marker()
        marker.position = LatLng(todo.center_lat,todo.center_lng)
        marker.map = naverMap

        val cameraUpdate = CameraUpdate.scrollTo(LatLng(todo.center_lat, todo.center_lng))
        naverMap.moveCamera(cameraUpdate)


    }


    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }


    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }



//    override fun onMapReady(p0: NaverMap) {
//        TODO("Not yet implemented")
//    }


}