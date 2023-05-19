package com.example.gymbeacon.ui.home.fragment

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.CompoundButton
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gymbeacon.R
import com.example.gymbeacon.ViewModelFactory
import com.example.gymbeacon.databinding.FragmentNaviMypageBinding
import com.example.gymbeacon.databinding.FragmentPartChartBinding
import com.example.gymbeacon.ui.chart.DateCountsData
import com.example.gymbeacon.ui.chart.MyMarkerView
import com.example.gymbeacon.ui.common.CommonUtil
import com.example.gymbeacon.ui.home.viewmodel.NaviMyPageViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class PartChartFragment : Fragment() {
    lateinit var binding: FragmentPartChartBinding
    private val viewModel: NaviMyPageViewModel by viewModels { ViewModelFactory() }

    private var selectedButton: Button? = null

    // bench 차트 데이터 ArrayList
    var barEntryArrayList_bench: ArrayList<BarEntry?>? = ArrayList()
    var labelsNames_bench = ArrayList<String>()
    var dateCountsDataArrayList_bench = ArrayList<DateCountsData>()

    // squat 차트 데이터 ArrayList
    var barEntryArrayList_squat: ArrayList<BarEntry?>? = ArrayList()
    var labelsNames_squat = ArrayList<String>()
    var dateCountsDataArrayList_squat = ArrayList<DateCountsData>()

    // dead 차트 데이터 ArrayList
    var barEntryArrayList_dead: ArrayList<BarEntry?>? = ArrayList()
    var labelsNames_dead = ArrayList<String>()
    var dateCountsDataArrayList_dead = ArrayList<DateCountsData>()

    // incline 차트 데이터 ArrayList
    var barEntryArrayList_incline: ArrayList<BarEntry?>? = ArrayList()
    var labelsNames_incline = ArrayList<String>()
    var dateCountsDataArrayList_incline = ArrayList<DateCountsData>()

    // legex 차트 데이터 ArrayList
    var barEntryArrayList_legex: ArrayList<BarEntry?>? = ArrayList()
    var labelsNames_legex = ArrayList<String>()
    var dateCountsDataArrayList_legex = ArrayList<DateCountsData>()

    // latpulldown 차트 데이터 ArrayList
    var barEntryArrayList_latpull: ArrayList<BarEntry?>? = ArrayList()
    var labelsNames_latpull = ArrayList<String>()
    var dateCountsDataArrayList_latpull = ArrayList<DateCountsData>()

    // 라인 차트 데이터
    var entry_bench = ArrayList<Entry>()
    var entry_squat = ArrayList<Entry>()
    var entry_dead = ArrayList<Entry>()
    var entry_incline = ArrayList<Entry>()
    var entry_legex = ArrayList<Entry>()
    var entry_latpull = ArrayList<Entry>()

    var lineData_bench = LineData()
    var lineData_squat = LineData()
    var lineData_dead = LineData()
    var lineData_incline = LineData()
    var lineData_legex = LineData()
    var lineData_latpull = LineData()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_part_chart, container, false)

        binding.partChartBtn.isSelected = true

        binding.totalChartBtn.setOnClickListener {

            binding.totalChartBtn.isSelected = true
            binding.partChartBtn.isSelected = false
            // 버튼 1 클릭 시 Fragment Total로 전환
            val fragmentTotal = NaviMyPageFragment()
            val fragmentTransaction = parentFragmentManager.beginTransaction()

            fragmentTransaction.replace(R.id.fragmentContainer, fragmentTotal)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()

        }

        // 버튼2 클릭 이벤트 리스너 설정
        binding.partChartBtn.setOnClickListener {
            // 이미 Fragment part Chart를 표시하고 있으므로 추가 작업이 필요하지 않음
            binding.partChartBtn.isSelected = true
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {


        CommonUtil.myRef.orderByChild("uid").equalTo(CommonUtil.mAuth.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    // 각 운동별 데이터 arrayList
                    dateCountsDataArrayList_bench.clear() // 리스트 초기화
                    barEntryArrayList_bench?.clear()
                    labelsNames_bench.clear()
                    dateCountsDataArrayList_squat.clear() // 리스트 초기화
                    barEntryArrayList_squat?.clear()
                    labelsNames_squat.clear()
                    dateCountsDataArrayList_dead.clear() // 리스트 초기화
                    barEntryArrayList_dead?.clear()
                    labelsNames_dead.clear()
                    dateCountsDataArrayList_incline.clear() // 리스트 초기화
                    barEntryArrayList_incline?.clear()
                    labelsNames_incline.clear()
                    dateCountsDataArrayList_legex.clear() // 리스트 초기화
                    barEntryArrayList_legex?.clear()
                    labelsNames_legex.clear()
                    dateCountsDataArrayList_latpull.clear() // 리스트 초기화
                    barEntryArrayList_latpull?.clear()
                    labelsNames_latpull.clear()

                    // 라인 차트 arrayList
                    entry_bench.clear()
                    entry_squat.clear()
                    entry_dead.clear()
                    entry_incline.clear()
                    entry_legex.clear()
                    entry_latpull.clear()
                    lineData_bench.clearValues()
                    lineData_squat.clearValues()
                    lineData_dead.clearValues()
                    lineData_incline.clearValues()
                    lineData_legex.clearValues()
                    lineData_latpull.clearValues()

                    for (postSnapshot: DataSnapshot in snapshot.children) {
                        val ex_name = postSnapshot.child("exercise").getValue(
                            String::class.java
                        )

                        // 운동 이름이 벤치프레스
                        if ("벤치프레스" == ex_name) {
                            //데이터 가져오기(count, timestamp 이름으로 된 값)
                            val date = postSnapshot.child("timestamp").getValue(
                                String::class.java
                            )
                            val counts = postSnapshot.child("count").getValue(
                                String::class.java
                            )
                            dateCountsDataArrayList_bench.add(DateCountsData(date, counts))

                        }
                        // 운동 이름이 스쿼트
                        if ("스쿼트" == ex_name) {
                            //데이터 가져오기(count, timestamp 이름으로 된 값)
                            val date = postSnapshot.child("timestamp").getValue(
                                String::class.java
                            )
                            val counts = postSnapshot.child("count").getValue(
                                String::class.java
                            )
                            Log.i("테스트1", "date= " + date)
                            Log.i("테스트1", "counts= " + counts)
                            dateCountsDataArrayList_squat.add(DateCountsData(date, counts))

                        }
                        // 운동 이름이 데드리프트
                        if ("데드리프트" == ex_name) {
                            //데이터 가져오기(count, timestamp 이름으로 된 값)
                            val date = postSnapshot.child("timestamp").getValue(
                                String::class.java
                            )
                            val counts = postSnapshot.child("count").getValue(
                                String::class.java
                            )
                            dateCountsDataArrayList_dead.add(DateCountsData(date, counts))

                        }
                        // 운동 이름이 인클라인 벤치프레스
                        if ("인클라인 벤치프레스" == ex_name) {
                            //데이터 가져오기(count, timestamp 이름으로 된 값)
                            val date = postSnapshot.child("timestamp").getValue(
                                String::class.java
                            )
                            val counts = postSnapshot.child("count").getValue(
                                String::class.java
                            )
                            dateCountsDataArrayList_incline.add(DateCountsData(date, counts))

                        }
                        // 운동 이름이 레그 익스텐션
                        if ("레그 익스텐션" == ex_name) {
                            //데이터 가져오기(count, timestamp 이름으로 된 값)
                            val date = postSnapshot.child("timestamp").getValue(
                                String::class.java
                            )
                            val counts = postSnapshot.child("count").getValue(
                                String::class.java
                            )
                            dateCountsDataArrayList_legex.add(DateCountsData(date, counts))

                        }
                        // 운동 이름이 랫 풀 다운
                        if ("랫 풀 다운" == ex_name) {
                            //데이터 가져오기(count, timestamp 이름으로 된 값)
                            val date = postSnapshot.child("timestamp").getValue(
                                String::class.java
                            )
                            val counts = postSnapshot.child("count").getValue(
                                String::class.java
                            )
                            dateCountsDataArrayList_latpull.add(DateCountsData(date, counts))

                        }

                    }

                    chartBench.let {
                        fillDateCounts(
                            it,
                            dateCountsDataArrayList_bench,
                            labelsNames_bench,
                            barEntryArrayList_bench
                        )
                    }
                    chartSquat.let {
                        fillDateCounts(
                            it,
                            dateCountsDataArrayList_squat,
                            labelsNames_squat,
                            barEntryArrayList_squat
                        )
                    }
                    chartDead.let {
                        fillDateCounts(
                            it,
                            dateCountsDataArrayList_dead,
                            labelsNames_dead,
                            barEntryArrayList_dead
                        )
                    }
                    chartIncline.let {
                        fillDateCounts(
                            it,
                            dateCountsDataArrayList_incline,
                            labelsNames_incline,
                            barEntryArrayList_incline
                        )
                    }
                    chartLegex.let {
                        fillDateCounts(
                            it,
                            dateCountsDataArrayList_legex,
                            labelsNames_legex,
                            barEntryArrayList_legex
                        )
                    }
                    chartLatpulldown.let {
                        fillDateCounts(
                            it,
                            dateCountsDataArrayList_latpull,
                            labelsNames_latpull,
                            barEntryArrayList_latpull
                        )
                    }


                    // 라인 차트
                    lineChartCreateData(binding.lineChartBench, labelsNames_bench, dateCountsDataArrayList_bench, entry_bench)
                    lineChartCreateData(binding.lineChartSquat, labelsNames_squat, dateCountsDataArrayList_squat, entry_squat)
                    lineChartCreateData(binding.lineChartDead, labelsNames_dead, dateCountsDataArrayList_dead, entry_dead)
                    lineChartCreateData(binding.lineChartIncline, labelsNames_incline, dateCountsDataArrayList_incline, entry_incline)
                    lineChartCreateData(binding.lineChartLegex, labelsNames_legex, dateCountsDataArrayList_legex, entry_legex)
                    lineChartCreateData(binding.lineChartLatpulldown, labelsNames_latpull, dateCountsDataArrayList_latpull, entry_latpull)

                    // 라인 차트//////////////////////////////////////////////

                } //onDataChange

                override fun onCancelled(error: DatabaseError) {} //onCancelled
            }) //addValueEventListener


            chartBench.setVisibility(View.INVISIBLE)
            chartSquat.setVisibility(View.INVISIBLE)
            chartDead.setVisibility(View.INVISIBLE)
            chartIncline.setVisibility(View.INVISIBLE)
            chartLegex.setVisibility(View.INVISIBLE)
            chartLatpulldown.setVisibility(View.INVISIBLE)
            chartNameText.setText("운동을 선택하세요.")
            lineChartBench.setVisibility(View.INVISIBLE)
            lineChartSquat.setVisibility(View.INVISIBLE)
            lineChartDead.setVisibility(View.INVISIBLE)
            lineChartIncline.setVisibility(View.INVISIBLE)
            lineChartLegex.setVisibility(View.INVISIBLE)
            lineChartLatpulldown.setVisibility(View.INVISIBLE)

            val animation = AlphaAnimation(0f, 1f)
            animation.duration = 1000

            benchBtn.setOnClickListener {
                chartBench.setVisibility(View.VISIBLE)
                chartBench.animation = animation
                chartSquat.setVisibility(View.INVISIBLE)
                chartDead.setVisibility(View.INVISIBLE)
                chartIncline.setVisibility(View.INVISIBLE)
                chartLegex.setVisibility(View.INVISIBLE)
                chartLatpulldown.setVisibility(View.INVISIBLE)
                chartNameText.setText("벤치프레스")
                lineChartBench.setVisibility(View.VISIBLE)
                lineChartBench.animation = animation
                lineChartSquat.setVisibility(View.INVISIBLE)
                lineChartDead.setVisibility(View.INVISIBLE)
                lineChartIncline.setVisibility(View.INVISIBLE)
                lineChartLegex.setVisibility(View.INVISIBLE)
                lineChartLatpulldown.setVisibility(View.INVISIBLE)

                benchBtn.isSelected = true
                squatBtn.isSelected = false
                deadBtn.isSelected = false
                inclineBtn.isSelected = false
                legexBtn.isSelected = false
                latpullBtn.isSelected = false
            }
            squatBtn.setOnClickListener {
                chartBench.setVisibility(View.INVISIBLE)
                chartSquat.setVisibility(View.VISIBLE)
                chartSquat.animation = animation
                chartDead.setVisibility(View.INVISIBLE)
                chartIncline.setVisibility(View.INVISIBLE)
                chartLegex.setVisibility(View.INVISIBLE)
                chartLatpulldown.setVisibility(View.INVISIBLE)
                chartNameText.setText("스쿼트")
                lineChartBench.setVisibility(View.INVISIBLE)
                lineChartSquat.setVisibility(View.VISIBLE)
                lineChartSquat.animation = animation
                lineChartDead.setVisibility(View.INVISIBLE)
                lineChartIncline.setVisibility(View.INVISIBLE)
                lineChartLegex.setVisibility(View.INVISIBLE)
                lineChartLatpulldown.setVisibility(View.INVISIBLE)

                benchBtn.isSelected = false
                squatBtn.isSelected = true
                deadBtn.isSelected = false
                inclineBtn.isSelected = false
                legexBtn.isSelected = false
                latpullBtn.isSelected = false
            }
            deadBtn.setOnClickListener {
                chartBench.setVisibility(View.INVISIBLE)
                chartSquat.setVisibility(View.INVISIBLE)
                chartDead.setVisibility(View.VISIBLE)
                chartDead.animation = animation
                chartIncline.setVisibility(View.INVISIBLE)
                chartLegex.setVisibility(View.INVISIBLE)
                chartLatpulldown.setVisibility(View.INVISIBLE)
                chartNameText.setText("데드리프트")
                lineChartBench.setVisibility(View.INVISIBLE)
                lineChartSquat.setVisibility(View.INVISIBLE)
                lineChartDead.setVisibility(View.VISIBLE)
                lineChartDead.animation = animation
                lineChartIncline.setVisibility(View.INVISIBLE)
                lineChartLegex.setVisibility(View.INVISIBLE)
                lineChartLatpulldown.setVisibility(View.INVISIBLE)

                benchBtn.isSelected = false
                squatBtn.isSelected = false
                deadBtn.isSelected = true
                inclineBtn.isSelected = false
                legexBtn.isSelected = false
                latpullBtn.isSelected = false
            }
            inclineBtn.setOnClickListener {
                chartBench.setVisibility(View.INVISIBLE)
                chartSquat.setVisibility(View.INVISIBLE)
                chartDead.setVisibility(View.INVISIBLE)
                chartIncline.setVisibility(View.VISIBLE)
                chartIncline.animation = animation
                chartLegex.setVisibility(View.INVISIBLE)
                chartLatpulldown.setVisibility(View.INVISIBLE)
                chartNameText.setText("인클라인 벤치프레스")
                lineChartBench.setVisibility(View.INVISIBLE)
                lineChartSquat.setVisibility(View.INVISIBLE)
                lineChartDead.setVisibility(View.INVISIBLE)
                lineChartIncline.setVisibility(View.VISIBLE)
                lineChartIncline.animation = animation
                lineChartLegex.setVisibility(View.INVISIBLE)
                lineChartLatpulldown.setVisibility(View.INVISIBLE)

                benchBtn.isSelected = false
                squatBtn.isSelected = false
                deadBtn.isSelected = false
                inclineBtn.isSelected = true
                legexBtn.isSelected = false
                latpullBtn.isSelected = false
            }
            legexBtn.setOnClickListener {
                chartBench.setVisibility(View.INVISIBLE)
                chartSquat.setVisibility(View.INVISIBLE)
                chartDead.setVisibility(View.INVISIBLE)
                chartIncline.setVisibility(View.INVISIBLE)
                chartLegex.setVisibility(View.VISIBLE)
                chartLegex.animation = animation
                chartLatpulldown.setVisibility(View.INVISIBLE)
                chartNameText.setText("레그 익스텐션")
                lineChartBench.setVisibility(View.INVISIBLE)
                lineChartSquat.setVisibility(View.INVISIBLE)
                lineChartDead.setVisibility(View.INVISIBLE)
                lineChartIncline.setVisibility(View.INVISIBLE)
                lineChartLegex.setVisibility(View.VISIBLE)
                lineChartLegex.animation = animation
                lineChartLatpulldown.setVisibility(View.INVISIBLE)

                benchBtn.isSelected = false
                squatBtn.isSelected = false
                deadBtn.isSelected = false
                inclineBtn.isSelected = false
                legexBtn.isSelected = true
                latpullBtn.isSelected = false
            }
            latpullBtn.setOnClickListener {
                chartBench.setVisibility(View.INVISIBLE)
                chartSquat.setVisibility(View.INVISIBLE)
                chartDead.setVisibility(View.INVISIBLE)
                chartIncline.setVisibility(View.INVISIBLE)
                chartLegex.setVisibility(View.INVISIBLE)
                chartLatpulldown.setVisibility(View.VISIBLE)
                chartLatpulldown.animation = animation
                chartNameText.setText("랫 풀 다운")
                lineChartBench.setVisibility(View.INVISIBLE)
                lineChartSquat.setVisibility(View.INVISIBLE)
                lineChartDead.setVisibility(View.INVISIBLE)
                lineChartIncline.setVisibility(View.INVISIBLE)
                lineChartLegex.setVisibility(View.INVISIBLE)
                lineChartLatpulldown.setVisibility(View.VISIBLE)
                lineChartLatpulldown.animation = animation

                benchBtn.isSelected = false
                squatBtn.isSelected = false
                deadBtn.isSelected = false
                inclineBtn.isSelected = false
                legexBtn.isSelected = false
                latpullBtn.isSelected = true
            }

    }

    }

    fun onButtonClick(view: View) {
        val clickedButton = view as Button

        // 선택된 버튼 설정
        clickedButton.isSelected = !clickedButton.isSelected

        // 선택 상태에 따라 배경색 변경
        if (clickedButton.isSelected) {
            clickedButton.setBackgroundColor(Color.RED)
        } else {
            clickedButton.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    fun fillDateCounts(
        barChart: BarChart,
        dateCountsDataArrayList: ArrayList<DateCountsData>,
        labelsNames: ArrayList<String>,
        barEntryArrayList: ArrayList<BarEntry?>?
    ) {
        for (i in dateCountsDataArrayList.indices) {
            var date = dateCountsDataArrayList[i].date
            var counts = dateCountsDataArrayList[i].counts

            barEntryArrayList?.add(BarEntry(i.toFloat(), counts.toFloat()))
            labelsNames.add(date)
        }

        val barDataSet = BarDataSet(barEntryArrayList, "날짜별 운동 횟수")
        barDataSet.valueFormatter = CountValueFormatter()

        barDataSet.color = Color.rgb(31, 120, 180)
        val description = Description()
        description.text = "날짜"
        barChart.description = description
        val barData = BarData(barDataSet)
        barChart.data = barData
        barChart.let { graphInitSetting(it, labelsNames, dateCountsDataArrayList) } // 차트 기본 세팅

        // 가장 최근에 추가한 데이터의 위치로 이동처리
        barChart.moveViewToX(barDataSet.entryCount.toFloat())
    }

    fun graphInitSetting(barChart: BarChart, labelsNames: ArrayList<String>, dateCountsDataArrayList: ArrayList<DateCountsData>) {

        // 배경 색
        barChart.setBackgroundColor(Color.parseColor("#E6E6FA"))
        // 그래프 터치 가능
        barChart.setTouchEnabled(true)
        // X축으로 드래그 가능
        barChart.isDragXEnabled = true
        // Y축으로 드래그 불가능
        barChart.isDragYEnabled = false
        // 확대 불가능
        barChart.setScaleEnabled(false)
        // pinch zoom 가능 (손가락으로 확대축소하는거)
        barChart.setPinchZoom(true)
        barChart.animateXY(1000, 1000)

        // 최대 x좌표 기준으로 몇개를 보여줄지 (최소값, 최대값)
        barChart.setVisibleXRange(1f, 5f)

        val xAxisLabels = ArrayList<String>()
        val inputFormat = SimpleDateFormat("yyyy-MM-dd")        // 년도
        val outputFormat = SimpleDateFormat("MM-dd")  // 월-일

        for (dateStr in labelsNames) {
            try {
                val date = inputFormat.parse(dateStr)
                val xAxisLabel = outputFormat.format(date)
                xAxisLabels.add(xAxisLabel)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }

        // x축 값 포맷
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
        //xAxis.valueFormatter = IndexAxisValueFormatter(labelsNames)

        // x축 라벨 네임 위치 지정
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelCount = xAxisLabels.size

        barChart.animateY(2000)
        barChart.invalidate()


        // y축 설정
        //var max_counts: Int = dateCountsDataArrayList.get(0).getCounts().toInt() // 최대 개수
        var countsList = ArrayList<Int>()

        for (dateCounts in dateCountsDataArrayList) {
            countsList.add(dateCounts.counts.toInt())
        }
        var max_counts = countsList.maxOrNull()

        val yAxis = barChart.axisLeft
        barChart.axisRight.isEnabled = false
        yAxis.axisMinimum = 0f
        if (max_counts != null) {
            yAxis.axisMaximum = max_counts.toFloat()
        }

        yAxis.granularity = 1f
        yAxis.valueFormatter = yAxisValueFormatter()
//        yAxis.spaceMax = 1f
//        yAxis.spaceMin = 1f
    }

    fun lineChartCreateData(lineChart: LineChart, labelsNames: ArrayList<String>, dateCountsDataArrayList: ArrayList<DateCountsData>, entries: ArrayList<Entry>) {

        for (i in dateCountsDataArrayList.indices) {
            var date = dateCountsDataArrayList[i].date
            var counts = dateCountsDataArrayList[i].counts

            entries.add(Entry(i.toFloat(), counts.toFloat()))
            labelsNames.add(date)
        }


        val lineDataSet = LineDataSet(entries, "날짜별 운동 횟수")

        lineDataSetSetting(lineDataSet, lineChart, labelsNames, dateCountsDataArrayList)
    }

    fun lineDataSetSetting(lineDataSet: LineDataSet, lineChart: LineChart, labelsNames: ArrayList<String>, dateCountsDataArrayList: ArrayList<DateCountsData>) {

        lineDataSet.color = Color.rgb(31, 120, 180)
        lineDataSet.setCircleColor(Color.rgb(31, 120, 180))

        val lineData = LineData(lineDataSet)
        lineChart.data = lineData
        lineChartSetting(lineChart, labelsNames, dateCountsDataArrayList)

//        lineDataSet.valueFormatter = CountValueFormatter()
        lineDataSet.lineWidth = 3f
        lineDataSet.circleRadius = 6f
        lineDataSet.setDrawValues(false)
        lineDataSet.setDrawCircleHole(true)
        lineDataSet.setDrawCircles(true)
        lineDataSet.setDrawHorizontalHighlightIndicator(false)
        lineDataSet.setDrawHighlightIndicators(false)

        // 가장 최근에 추가한 데이터의 위치로 이동처리
        lineChart.moveViewToX(lineDataSet.entryCount.toFloat())

    }

    fun lineChartSetting(lineChart: LineChart, labelsNames: ArrayList<String>, dateCountsDataArrayList: ArrayList<DateCountsData>) {
        lineChart.extraBottomOffset = 15f // 간격
        lineChart.description.isEnabled = false // chart 밑에 description 표시 유무
        lineChart.setBackgroundColor(Color.parseColor("#E6E6FA"))      // 배경색
        lineChart.setVisibleXRange(1f, 5f)
        lineChart.animateXY(1000, 1000)

        val markerview = MyMarkerView(context,  R.layout.custom_marker_view)
        markerview.chartView = lineChart
        lineChart.marker = markerview

        // Legend는 차트의 범례
//        val legend = lineChart.legend
//        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
//        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
//        legend.form = Legend.LegendForm.CIRCLE
//        legend.formSize = 10f
//        legend.textSize = 13f
//        legend.textColor = Color.parseColor("#A3A3A3")
//        legend.orientation = Legend.LegendOrientation.VERTICAL
//        //legend.isDrawInside = false
//        legend.yEntrySpace = 5f
//        legend.isWordWrapEnabled = true
//        legend.xOffset = 80f
//        legend.yOffset = 20f
//        legend.calculatedLineSizes

        // XAxis (아래쪽) - 선 유무, 사이즈, 색상, 축 위치 설정
        val xAxis = lineChart.xAxis

        val xAxisLabels = ArrayList<String>()
        val inputFormat = SimpleDateFormat("yyyy-MM-dd")        // 년도
        val outputFormat = SimpleDateFormat("MM-dd")            // 월-일

        for (dateStr in labelsNames) {
            try {
                val date = inputFormat.parse(dateStr)
                val xAxisLabel = outputFormat.format(date)
                xAxisLabels.add(xAxisLabel)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }

        xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
        xAxis.labelCount = xAxisLabels.size

        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM // x축 데이터 표시 위치
        xAxis.granularity = 1f
        xAxis.textColor = Color.rgb(118, 118, 118)
        xAxis.spaceMin = 1f // Chart 맨 왼쪽 간격 띄우기
        xAxis.spaceMax = 1f // Chart 맨 오른쪽 간격 띄우기


        // YAxis(Right) (왼쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.textColor = Color.BLACK
        yAxisLeft.setDrawAxisLine(true)
        yAxisLeft.axisLineWidth = 2f
        yAxisLeft.axisMinimum = 0f // 최솟값
//        yAxisLeft.axisMaximum = RANGE[0][range].toFloat() // 최댓값
//        yAxisLeft.granularity = RANGE[1][range].toFloat()
        var countsList = ArrayList<Int>()

        for (dateCounts in dateCountsDataArrayList) {
            countsList.add(dateCounts.counts.toInt())
        }
        var max_counts = countsList.maxOrNull()

        if (max_counts != null) {
            yAxisLeft.axisMaximum = max_counts.toFloat()
        }
        yAxisLeft.granularity = 1f

        // YAxis(Left) (오른쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
        val yAxis = lineChart.axisRight
        yAxis.setDrawLabels(true) // label 삭제
        yAxis.textColor = Color.rgb(163, 163, 163)
        yAxis.setDrawAxisLine(false)
        yAxis.axisLineWidth = 2f
        yAxis.axisMinimum = 0f // 최솟값
        if (max_counts != null) {
            yAxis.axisMaximum = max_counts.toFloat()
        }
        yAxis.granularity = 1f

    }

}
