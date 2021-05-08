package com.example.todowhere

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

// Realm 모델 클래스
open class Todo (
    // id, 무엇을 할지, 언제, 어디서 할지
    @PrimaryKey var id: Long = 0 ,
    var what_todo: String = "" ,
    var date: Long = 0 ,
    var where_todo: String = ""
)  : RealmObject() {}
