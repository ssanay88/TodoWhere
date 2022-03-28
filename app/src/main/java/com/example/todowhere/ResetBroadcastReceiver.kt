package com.example.todowhere

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// 매일 리셋해야하는 행동
// 1. 완료하지 못한 할 일 들 모두 'Finish' 처리
// 2. Todo리스트와 지오펜싱 리스트 클리어 이후 다음날짜의 Todo와 지오펜싱 추가
class ResetBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

    }


}