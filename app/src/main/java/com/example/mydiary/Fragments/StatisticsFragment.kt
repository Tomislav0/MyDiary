package com.example.mydiary.Fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.mydiary.R
import com.example.mydiary.constants.Constants
import com.example.mydiary.constants.Constants.ChartMaterials
import com.example.mydiary.helpers.DateHelper
import com.example.mydiary.models.NoteBM
import com.example.mydiary.models.NoteDto
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.security.acl.Group
import java.sql.Date
import java.sql.Timestamp
import java.time.Instant
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics), View.OnClickListener {
    @Inject
    lateinit var dateHelper: DateHelper

    lateinit var auth : FirebaseAuth
    lateinit var database : FirebaseDatabase
    lateinit var gson: Gson

    lateinit var barChart:BarChart

    lateinit var datePeriodTV:TextView
    lateinit var imagesNumberTV:TextView
    lateinit var voiceRecordingsNumberTV:TextView
    lateinit var notesNumberTV:TextView
    lateinit var progressBar: ProgressBar
    lateinit var group:androidx.constraintlayout.widget.Group
    lateinit var noDataTV: TextView
    var datefilter :java.util.Date?= null
    lateinit var barEntry : ArrayList<BarEntry>
    lateinit var datesModel : ArrayList<String>
    lateinit var radioAllButton: RadioButton
    lateinit var radioYearButton: RadioButton
    lateinit var radioMonthButton: RadioButton
    lateinit var fview: View

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fview =view

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance(Constants.BASE_URL)
        gson=Gson()

        datePeriodTV = view.findViewById(R.id.datePeriodTV)
        imagesNumberTV = view.findViewById(R.id.imagesNumberTV)
        voiceRecordingsNumberTV = view.findViewById(R.id.voiceRecordingsNumberTV)
        notesNumberTV = view.findViewById(R.id.notesNumberTV)
        progressBar = view.findViewById(R.id.statisticsPB)
        group = view.findViewById(R.id.statisticGroup)
        noDataTV = view.findViewById(R.id.noDataText)
        barChart = view.findViewById(R.id.barChart)

        radioAllButton = view.findViewById(R.id.radio_all)
        radioYearButton = view.findViewById(R.id.radio_year)
        radioMonthButton = view.findViewById(R.id.radio_month)

        radioAllButton.setOnClickListener(this)
        radioYearButton.setOnClickListener(this)
        radioMonthButton.setOnClickListener(this)



        group.visibility =View.INVISIBLE
        progressBar.visibility = View.VISIBLE
        radioAllButton.callOnClick()
        radioAllButton.isChecked=true


    }

    private fun fetchData(view: View) {
        database.getReference("Notes").child(auth.currentUser!!.uid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                var i = 0.5
                var colors = arrayListOf<Int>()
                var imagesCount = 0
                var voiceRecordingsCount = 0

                var notes = arrayListOf<NoteDto>()
                it.result.children.forEach { data ->
                    val toJson = gson.toJson(data.value)
                    val note = gson.fromJson(toJson, NoteBM::class.java)
                    val parsedDate = dateHelper.convertToDateFormat(note.date)
                    notes.add(
                        NoteDto(
                            note.id,
                            parsedDate,
                            note.title,
                            note.content,
                            note.hasImage,
                            note.hasVoiceRecording,
                            note.moodRate
                        )
                    )
                }
                notes.sortBy { i -> i.date }
                if(datefilter != null){
                 notes = notes.filter { i -> i.date >= datefilter } as ArrayList<NoteDto>
                }

                notes.forEach { note ->
                    val parsedDate = dateHelper.getDateChartFormat(note.date)
                    if (note.hasImage) {
                        imagesCount++
                    }
                    if (note.hasVoiceRecording) {
                        voiceRecordingsCount++
                    }

                    barEntry.add(BarEntry(i.toFloat(), note.moodRate.toFloat()))
                    datesModel.add(parsedDate)
                    i++

                    when (note.moodRate) {
                        1 -> {
                            colors.add(ColorTemplate.rgb("#22c1c3"))
                        }
                        2 -> {
                            colors.add(ColorTemplate.rgb("#3ac1b3"))
                        }
                        3 -> {
                            colors.add(ColorTemplate.rgb("#52c0a2"))
                        }
                        4 -> {
                            colors.add(ColorTemplate.rgb("#66bf95"))
                        }
                        5 -> {
                            colors.add(ColorTemplate.rgb("#77bf89"))
                        }
                        6 -> {
                            colors.add(ColorTemplate.rgb("#94be75"))
                        }
                        7 -> {
                            colors.add(ColorTemplate.rgb("#b5bd5f"))
                        }
                        8 -> {
                            colors.add(ColorTemplate.rgb("#d1bc4b"))
                        }
                        9 -> {
                            colors.add(ColorTemplate.rgb("#e9bb3b"))
                        }
                        10 -> {
                            colors.add(ColorTemplate.rgb("#fdbb2d"))
                        }
                    }
                }
                if (barEntry.isEmpty()) {
                    noDataTV.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                } else {
                    datePeriodTV.text = if (datesModel.first().takeLast(5) == datesModel.last()
                            .takeLast(5)
                    ) "${
                        datesModel.first().subSequence(0, 5)
                    } - ${datesModel.last()}" else "${datesModel.first()} - ${datesModel.last()}"
                    notesNumberTV.text =
                        if (barEntry.size != 1) "${barEntry.size} notes" else "${barEntry.size} note"
                    imagesNumberTV.text =
                        if (imagesCount != 1) "$imagesCount images" else "$imagesCount image"
                    voiceRecordingsNumberTV.text =
                        if (voiceRecordingsCount != 1) "$voiceRecordingsCount voice recordings" else "$voiceRecordingsCount voice recording"
                    val first = datesModel.first()
                    val mid = datesModel[(datesModel.size - 1) / 2]
                    val last = datesModel.last()
                    if (notes.size > 4) {
                        datesModel.replaceAll { i -> "" }
                        datesModel[0] = first
                        datesModel[(datesModel.size - 1) / 2] = mid
                        datesModel[datesModel.lastIndex] = last
                    }
                    val barDataSet = BarDataSet(barEntry, "Mood chart")
                    barDataSet.setColors(colors)
                    barDataSet.valueTextSize = 15f
                    barDataSet.setDrawValues(false)

                    val barData = BarData(barDataSet)

                    barChart.animateY(1100)
                    barChart.data = barData
                    barChart.isClickable = false
                    barChart.isContextClickable = false
                    barChart.description.isEnabled = false
                    barChart.legend.isEnabled = false
                    barChart.xAxis.valueFormatter = IndexAxisValueFormatter(datesModel)
                    barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                    barChart.xAxis.setLabelCount(datesModel.size)
                    barChart.xAxis.setCenterAxisLabels(true)
                    barChart.setFitBars(true)
                    barChart.xAxis.textSize = 12f
                    barChart.xAxis.setAvoidFirstLastClipping(true)
                    barChart.xAxis.setCenterAxisLabels(true)

                    group.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }
            } else {
                Toast.makeText(view.context, "Something went wrong.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.radio_all ->{
                barEntry = arrayListOf()
                datesModel = arrayListOf()
                    datefilter = null
                    fetchData(fview)
                }
            R.id.radio_year ->{
                barEntry = arrayListOf()
                datesModel = arrayListOf()
                    val cal = Calendar.getInstance()
                    val year = cal.get(Calendar.YEAR)
                    cal.set(Calendar.YEAR, year-1)
                    datefilter = cal.time
                    fetchData(fview)
                }
            R.id.radio_month -> {
                barEntry = arrayListOf()
                datesModel = arrayListOf()
                    val cal = Calendar.getInstance()
                    val month = cal.get(Calendar.MONTH)
                    cal.set(Calendar.MONTH, month-1)
                    datefilter = cal.time
                    fetchData(fview)
                }
        }
    }

}