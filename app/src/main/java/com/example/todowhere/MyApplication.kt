package com.example.todowhere

import android.app.Application
import android.content.Context
import io.realm.Realm
// Application 클래스를 상속받는 MyApplication 선언
class MyApplication : Application() {
    // onCreate() 메서드를 오버라이드, 액티비티가 생성되기 전에 호출
    override fun onCreate() {
        super.onCreate()
        // Realm.init() 메서드를 사용하여 초기화
        Realm.init(this)
    }

    init {
        instance = this
    }

    companion object {
        lateinit var instance: MyApplication
        fun ApplicationContext() : Context {
            return instance.applicationContext
        }
    }
}