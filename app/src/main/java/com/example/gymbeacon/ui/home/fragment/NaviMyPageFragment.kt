package com.example.gymbeacon.ui.home.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.gymbeacon.R
import com.example.gymbeacon.ViewModelFactory
import com.example.gymbeacon.databinding.FragmentNaviMypageBinding
import com.example.gymbeacon.ui.chart.ChartActivity
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
import com.google.firebase.database.snapshot.Index
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class NaviMyPageFragment : Fragment() {
    lateinit var binding: FragmentNaviMypageBinding
    lateinit var viewPager: ViewPager2
    lateinit var viewPagerExerciseCountMap: MutableMap<String, Pair<Int,Int>>
    var returnCount = 1
    private val viewModel: NaviMyPageViewModel by viewModels { ViewModelFactory() }


    // bench 차트 데이터 ArrayList
    var barEntryArrayList_bench: ArrayList<BarEntry?>? = ArrayList()
    var labelsNames_bench = ArrayList<String>()
    var dateCountsDataArrayList_bench = ArrayList<DateCountsData>()

//    // squat 차트 데이터 ArrayList
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

    // crossover 차트 데이터 ArrayList
    var barEntryArrayList_crossover: ArrayList<BarEntry?>? = ArrayList()
    var labelsNames_crossover = ArrayList<String>()
    var dateCountsDataArrayList_crossover = ArrayList<DateCountsData>()

    // 레이더차트에 필요한 운동별 count 사이즈
    var counts_bench = 0
    var counts_squat = 0
    var counts_dead = 0
    var counts_incline = 0
    var counts_legex = 0
    var counts_crossover = 0

    val radarDataArrayList = ArrayList<RadarEntry>()

    // 파이 차트 데이터
    val pieEntryArrayList = ArrayList<PieEntry>()
    var counts_lower = 0
    var counts_back = 0
    var counts_chest = 0

    // 라인 차트 데이터
    var entry_bench = ArrayList<Entry>()
    var entry_squat = ArrayList<Entry>()
    var entry_dead = ArrayList<Entry>()
    var entry_incline = ArrayList<Entry>()
    var entry_legex = ArrayList<Entry>()
    var entry_crossover = ArrayList<Entry>()

    var lineData_bench = LineData()
    var lineData_squat = LineData()
    var lineData_dead = LineData()
    var lineData_incline = LineData()
    var lineData_legex = LineData()
    var lineData_crossover = LineData()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_navi_mypage, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dateFormat: DateFormat = SimpleDateFormat("yyyy년 MM월 dd일")

        with(binding) {
            chartBtn.setOnClickListener {
                goToChartActivity()
            }

        //  실시간 DB 참조 위치(health/momentum) 설정
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
                        dateCountsDataArrayList_crossover.clear() // 리스트 초기화
                        barEntryArrayList_crossover?.clear()
                        labelsNames_crossover.clear()

                        // 레이더 차트 arrayList
                        counts_bench = 0
                        counts_squat = 0
                        counts_dead = 0
                        counts_incline = 0
                        counts_legex = 0
                        counts_crossover = 0

                        radarDataArrayList.clear()

                        // 파이 차트 arrayList
                        pieEntryArrayList.clear()
                        counts_back = 0
                        counts_lower = 0
                        counts_chest = 0

                        // 라인 차트 arrayList
                        entry_bench.clear()
                        entry_squat.clear()
                        entry_dead.clear()
                        entry_incline.clear()
                        entry_legex.clear()
                        entry_crossover.clear()
                        lineData_bench.clearValues()
                        lineData_squat.clearValues()
                        lineData_dead.clearValues()
                        lineData_incline.clearValues()
                        lineData_legex.clearValues()
                        lineData_crossover.clearValues()

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

                                counts_bench += counts!!.toInt()
                                counts_chest += counts!!.toInt()
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

                                counts_squat += counts!!.toInt()
                                counts_lower += counts!!.toInt()
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

                                counts_dead += counts!!.toInt()
                                counts_back += counts!!.toInt()
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

                                counts_incline += counts!!.toInt()
                                counts_chest += counts!!.toInt()
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

                                counts_legex += counts!!.toInt()
                                counts_lower += counts!!.toInt()
                            }
                            // 운동 이름이 케이블 크로스오버
                            if ("케이블 크로스오버" == ex_name) {
                                //데이터 가져오기(count, timestamp 이름으로 된 값)
                                val date = postSnapshot.child("timestamp").getValue(
                                    String::class.java
                                )
                                val counts = postSnapshot.child("count").getValue(
                                    String::class.java
                                )
                                dateCountsDataArrayList_crossover.add(DateCountsData(date, counts))

                                counts_crossover += counts!!.toInt()
                                counts_chest += counts!!.toInt()
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
                        chartCrossover.let {
                            fillDateCounts(
                                it,
                                dateCountsDataArrayList_crossover,
                                labelsNames_crossover,
                                barEntryArrayList_crossover
                            )
                        }

                        // 레이더 차트
                        radarDataArrayList.add(RadarEntry(counts_bench.toFloat()))
                        radarDataArrayList.add(RadarEntry(counts_squat.toFloat()))
                        radarDataArrayList.add(RadarEntry(counts_dead.toFloat()))
                        radarDataArrayList.add(RadarEntry(counts_incline.toFloat()))
                        radarDataArrayList.add(RadarEntry(counts_legex.toFloat()))
                        radarDataArrayList.add(RadarEntry(counts_crossover.toFloat()))

                        val radarDataSet = RadarDataSet(radarDataArrayList, "운동 횟수")
                        radarDataSet.color = Color.BLUE
                        radarDataSet.valueFormatter = CountValueFormatter()     // "10개" 형식으로 변환

                        val radarData = RadarData()
                        radarData.addDataSet(radarDataSet)
                        val radarLabels = arrayOf("벤치프레스", "스쿼트", "데드리프트", "인클라인 벤치프레스", "레그 익스텐션", "케이블 크로스오버")
                        val xAxis_radar = binding.radarChart.xAxis
                        xAxis_radar.valueFormatter = IndexAxisValueFormatter(radarLabels)
                        binding.radarChart.data = radarData

                        val description_radar = Description()
                        description_radar.text = ""
                        description_radar.textSize = 15f
                        binding.radarChart.description = description_radar

                        binding.radarChart.invalidate()
                        // 레이더차트////////////////////////////////////////////////////////////////

                        // 파이 차트
                        if (counts_back != 0) {
                            pieEntryArrayList.add(PieEntry(counts_back.toFloat(), "등"))
                        }
                        if (counts_lower != 0) {
                            pieEntryArrayList.add(PieEntry(counts_lower.toFloat(), "하체"))
                        }
                        if (counts_chest != 0) {
                            pieEntryArrayList.add(PieEntry(counts_chest.toFloat(), "가슴"))
                        }

                        val pieDataSet = PieDataSet(pieEntryArrayList, " ← 부위 ")
                        pieDataSet.valueTextSize = 18f
                        if (pieEntryArrayList != null) {
                            val pieChartColors =
                                ColorTemplate.COLORFUL_COLORS.copyOf(pieEntryArrayList.size).toList()
                            pieDataSet.colors = pieChartColors
                        }

                        val description_pie = Description()
                        description_pie.text = ""
                        description_pie.textSize = 15f
                        binding.pieChart.description = description_pie

                        val pieData = PieData(pieDataSet)
                        pieDataSet.valueFormatter = PercentFormatter(binding.pieChart)
                        pieDataSet.valueTextSize = 18f

                        binding.pieChart.data = pieData

                        binding.pieChart.animateXY(1000, 1000)
                        binding.pieChart.setUsePercentValues(true)
                        // 파이 차트//////////////////////////////////////////////

                        // 라인 차트
                        lineChartCreateData(binding.lineChartBench, labelsNames_bench, dateCountsDataArrayList_bench, entry_bench)
                        lineChartCreateData(binding.lineChartSquat, labelsNames_squat, dateCountsDataArrayList_squat, entry_squat)
                        lineChartCreateData(binding.lineChartDead, labelsNames_dead, dateCountsDataArrayList_dead, entry_dead)
                        lineChartCreateData(binding.lineChartIncline, labelsNames_incline, dateCountsDataArrayList_incline, entry_incline)
                        lineChartCreateData(binding.lineChartLegex, labelsNames_legex, dateCountsDataArrayList_legex, entry_legex)
                        lineChartCreateData(binding.lineChartCrossover, labelsNames_crossover, dateCountsDataArrayList_crossover, entry_crossover)

                        // 라인 차트//////////////////////////////////////////////

                    } //onDataChange

                    override fun onCancelled(error: DatabaseError) {} //onCancelled
                }) //addValueEventListener

            chartBench.setVisibility(View.INVISIBLE)
            chartSquat.setVisibility(View.INVISIBLE)
            chartDead.setVisibility(View.INVISIBLE)
            chartIncline.setVisibility(View.INVISIBLE)
            chartLegex.setVisibility(View.INVISIBLE)
            chartCrossover.setVisibility(View.INVISIBLE)
            chartNameText.setText("운동을 선택하세요.")
            lineChartBench.setVisibility(View.INVISIBLE)
            lineChartSquat.setVisibility(View.INVISIBLE)
            lineChartDead.setVisibility(View.INVISIBLE)
            lineChartIncline.setVisibility(View.INVISIBLE)
            lineChartLegex.setVisibility(View.INVISIBLE)
            lineChartCrossover.setVisibility(View.INVISIBLE)

            val animation = AlphaAnimation(0f, 1f)
            animation.duration = 1000

            benchBtn.setOnClickListener {
                chartBench.setVisibility(View.VISIBLE)
                chartBench.animation = animation
                chartSquat.setVisibility(View.INVISIBLE)
                chartDead.setVisibility(View.INVISIBLE)
                chartIncline.setVisibility(View.INVISIBLE)
                chartLegex.setVisibility(View.INVISIBLE)
                chartCrossover.setVisibility(View.INVISIBLE)
                chartNameText.setText("벤치프레스")
                lineChartBench.setVisibility(View.VISIBLE)
                lineChartBench.animation = animation
                lineChartSquat.setVisibility(View.INVISIBLE)
                lineChartDead.setVisibility(View.INVISIBLE)
                lineChartIncline.setVisibility(View.INVISIBLE)
                lineChartLegex.setVisibility(View.INVISIBLE)
                lineChartCrossover.setVisibility(View.INVISIBLE)
            }
            squatBtn.setOnClickListener {
                chartBench.setVisibility(View.INVISIBLE)
                chartSquat.setVisibility(View.VISIBLE)
                chartSquat.animation = animation
                chartDead.setVisibility(View.INVISIBLE)
                chartIncline.setVisibility(View.INVISIBLE)
                chartLegex.setVisibility(View.INVISIBLE)
                chartCrossover.setVisibility(View.INVISIBLE)
                chartNameText.setText("스쿼트")
                lineChartBench.setVisibility(View.INVISIBLE)
                lineChartSquat.setVisibility(View.VISIBLE)
                lineChartSquat.animation = animation
                lineChartDead.setVisibility(View.INVISIBLE)
                lineChartIncline.setVisibility(View.INVISIBLE)
                lineChartLegex.setVisibility(View.INVISIBLE)
                lineChartCrossover.setVisibility(View.INVISIBLE)
            }
            deadBtn.setOnClickListener {
                chartBench.setVisibility(View.INVISIBLE)
                chartSquat.setVisibility(View.INVISIBLE)
                chartDead.setVisibility(View.VISIBLE)
                chartDead.animation = animation
                chartIncline.setVisibility(View.INVISIBLE)
                chartLegex.setVisibility(View.INVISIBLE)
                chartCrossover.setVisibility(View.INVISIBLE)
                chartNameText.setText("데드리프트")
                lineChartBench.setVisibility(View.INVISIBLE)
                lineChartSquat.setVisibility(View.INVISIBLE)
                lineChartDead.setVisibility(View.VISIBLE)
                lineChartDead.animation = animation
                lineChartIncline.setVisibility(View.INVISIBLE)
                lineChartLegex.setVisibility(View.INVISIBLE)
                lineChartCrossover.setVisibility(View.INVISIBLE)
            }
            inclineBtn.setOnClickListener {
                chartBench.setVisibility(View.INVISIBLE)
                chartSquat.setVisibility(View.INVISIBLE)
                chartDead.setVisibility(View.INVISIBLE)
                chartIncline.setVisibility(View.VISIBLE)
                chartIncline.animation = animation
                chartLegex.setVisibility(View.INVISIBLE)
                chartCrossover.setVisibility(View.INVISIBLE)
                chartNameText.setText("인클라인 벤치프레스")
                lineChartBench.setVisibility(View.INVISIBLE)
                lineChartSquat.setVisibility(View.INVISIBLE)
                lineChartDead.setVisibility(View.INVISIBLE)
                lineChartIncline.setVisibility(View.VISIBLE)
                lineChartIncline.animation = animation
                lineChartLegex.setVisibility(View.INVISIBLE)
                lineChartCrossover.setVisibility(View.INVISIBLE)
            }
            legexBtn.setOnClickListener {
                chartBench.setVisibility(View.INVISIBLE)
                chartSquat.setVisibility(View.INVISIBLE)
                chartDead.setVisibility(View.INVISIBLE)
                chartIncline.setVisibility(View.INVISIBLE)
                chartLegex.setVisibility(View.VISIBLE)
                chartLegex.animation = animation
                chartCrossover.setVisibility(View.INVISIBLE)
                chartNameText.setText("레그 익스텐션")
                lineChartBench.setVisibility(View.INVISIBLE)
                lineChartSquat.setVisibility(View.INVISIBLE)
                lineChartDead.setVisibility(View.INVISIBLE)
                lineChartIncline.setVisibility(View.INVISIBLE)
                lineChartLegex.setVisibility(View.VISIBLE)
                lineChartLegex.animation = animation
                lineChartCrossover.setVisibility(View.INVISIBLE)
            }
            crossoverBtn.setOnClickListener {
                chartBench.setVisibility(View.INVISIBLE)
                chartSquat.setVisibility(View.INVISIBLE)
                chartDead.setVisibility(View.INVISIBLE)
                chartIncline.setVisibility(View.INVISIBLE)
                chartLegex.setVisibility(View.INVISIBLE)
                chartCrossover.setVisibility(View.VISIBLE)
                chartCrossover.animation = animation
                chartNameText.setText("케이블 크로스오버")
                lineChartBench.setVisibility(View.INVISIBLE)
                lineChartSquat.setVisibility(View.INVISIBLE)
                lineChartDead.setVisibility(View.INVISIBLE)
                lineChartIncline.setVisibility(View.INVISIBLE)
                lineChartLegex.setVisibility(View.INVISIBLE)
                lineChartCrossover.setVisibility(View.VISIBLE)
                lineChartCrossover.animation = animation
            }

        }

    }

    fun setData(year: Int, month: Int, dayOfMonth: Int) {
        var customMonth = ""
        if (month < 10) {
            customMonth = "0" + month.toString()
        } else {
            customMonth = month.toString()
        }
        val nowTimeStamp = year.toString() + "-" + customMonth + "-" + dayOfMonth.toString()
        val sumCount = mutableListOf<Int>()

        viewModel.getDbData(nowTimeStamp)
        var exerciseCountMap = mutableMapOf<String, Int>()
        exerciseCountMap = viewModel.getExerciseCountMap()

    }

    fun goToChartActivity() {
        Intent(activity, ChartActivity::class.java).also { startActivity(it) }
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

        val barDataSet = BarDataSet(barEntryArrayList, "날짜별 부위별 운동 개수")
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
        barChart.setBackgroundColor(Color.rgb(254, 247, 235))
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
        barChart.setVisibleXRange(1f, 8f)

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


        val lineDataSet = LineDataSet(entries, "운동량")

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
        lineChart.setBackgroundColor(Color.rgb(254, 247, 235))      // 배경색
        lineChart.setVisibleXRange(1f, 8f)
        lineChart.animateXY(1000, 1000)

        val markerview = MyMarkerView(context,  R.layout.custom_marker_view)
        markerview.chartView = lineChart
        lineChart.marker = markerview

        // Legend는 차트의 범례
        val legend = lineChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.form = Legend.LegendForm.CIRCLE
        legend.formSize = 10f
        legend.textSize = 13f
        legend.textColor = Color.parseColor("#A3A3A3")
        legend.orientation = Legend.LegendOrientation.VERTICAL
        //legend.isDrawInside = false
        legend.yEntrySpace = 5f
        legend.isWordWrapEnabled = true
        legend.xOffset = 80f
        legend.yOffset = 20f
        legend.calculatedLineSizes

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

    companion object {
        fun newInstance(): NaviMyPageFragment {
            val args = Bundle().apply {
            }
            val fragment = NaviMyPageFragment()
            fragment.arguments = args
            return fragment
        }
    }
}

class CountValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return String.format(Locale.getDefault(), "%.0f회", value)
    }
}

class yAxisValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return String.format(Locale.getDefault(), "%.0f", value)
    }
}