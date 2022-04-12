package com.example.todowhere.CalendarDecorator

import android.content.Context
import com.example.todowhere.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class TodayDecorator(context: Context): DayViewDecorator {

    private val todayDate = CalendarDay.today()    // 오늘 날짜를 변수에 저장
    val backGroundDrawable = context.resources.getDrawable(R.drawable.today_background)

    // 데코레이트를 해야하는가?
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return todayDate?.equals(day)    // 비교 날짜가 오늘 날짜와 같으면 true 반환
    }

    // 데코레이트 설정
    override fun decorate(view: DayViewFacade?) {
        view?.setBackgroundDrawable(backGroundDrawable)    // 백그라운드 설정
    }
}