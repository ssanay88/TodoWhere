package com.example.todowhere.data

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

// Realm 모델 클래스
open class Todo  : RealmObject() {
    // id, 무엇을 할지, 언제, 어디서 할지
    @PrimaryKey var id: String = ""
    var what: String = ""
    var time: Long = 0
    var center_lat: Double = 0.0
    var center_lng: Double = 0.0
    var view_type: Int = 0
}

