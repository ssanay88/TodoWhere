package com.example.todowhere.RealmDB

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

// 지오펜싱 Realm 모델 클래스
open class Geofencing : RealmObject() {
    @PrimaryKey var id: String = ""
    var lat: Double = 0.0
    var lng: Double = 0.0
    // id를 통해 오늘 날짜에 해당하는 지오펜싱만 리스트에 담아서 사용


}