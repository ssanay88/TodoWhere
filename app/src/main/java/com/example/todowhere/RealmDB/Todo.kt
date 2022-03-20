package com.example.todowhere.RealmDB

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

// Realm 모델 클래스
open class Todo  : RealmObject() {
    // id, 무엇을 할지, 언제, 어디서 할지
    @PrimaryKey var id: String = ""
    var what: String = ""
    var time: Long = 0
    var center_lat: Double = 0.0
    var center_lng: Double = 0.0
    var view_type: Int = 0    // 일정이 없는 경우 0 , 일정을 모두 추가한 경우 1
    var state: String = "Wait"
    // 우선 대기로 설정하고 지오펜싱과 오늘 날짜를 통해 상태 변경
    // Done : 목표 달성 시 , Doing : 오늘 날짜에 할 일 진행중 , Stop : 오늘 날짜에 지오펜스 밖이라서 일시 정지
    // Wait : 미래의 할 일 , Finish : 목표 실패 및 날짜 지난 경우
}

