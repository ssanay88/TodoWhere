package com.example.todowhere

import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.overlay.CircleOverlay
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

// Realm 모델 클래스
open class Todo (
    // id, 무엇을 할지, 언제, 어디서 할지
    @PrimaryKey var id: String = "" ,
    var what: String = "" ,
    var time: Long = 0 ,
    var where: LatLngBounds ?= null
)  : RealmObject() {}
