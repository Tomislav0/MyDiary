package com.example.mydiary.helpers

import android.os.Build
import android.util.Log
import com.example.mydiary.constants.Constants.DATE_FORMAT
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime.ofInstant
import java.time.LocalTime.ofInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

class DateHelper @Inject constructor() {
    private val sdf: SimpleDateFormat = SimpleDateFormat(DATE_FORMAT)

    fun getCurrentTime(): String {
        return sdf.format(Date())
    }

    fun convertToDateFormat(date: String):Date{
        return sdf.parse(date)
    }

    fun convertToStringFormat(date:Date):String{
        return sdf.format(date)
    }

    fun getDateDay(date:Date): Int {
        val sdfDay = SimpleDateFormat("dd")
        return sdfDay.format(date).toInt()
    }

    fun getDateMonth(date:Date): Int {
        val sdfMonth = SimpleDateFormat("MM")
        return sdfMonth.format(date).toInt()
    }

    fun getDateYear(date:Date): Int {
        val sdfYear = SimpleDateFormat("yyyy")
        return sdfYear.format(date).toInt()
    }

    fun getDateChartFormat(date:Date): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy.")
        return sdf.format(date).toString()
    }
    fun getDateChartFormat(date:String): Date {
        val sdf = SimpleDateFormat("dd.MM.yyyy.")
        return sdf.parse(date)
    }
}