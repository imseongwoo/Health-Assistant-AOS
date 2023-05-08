package com.example.gymbeacon.ui.home.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.gymbeacon.ui.common.CommonUtil
import com.example.gymbeacon.ui.home.viewmodel.NaviMyPageViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.DateFormat
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

                        counts_bench = 0
                        counts_squat = 0
                        counts_dead = 0
                        counts_incline = 0
                        counts_legex = 0
                        counts_crossover = 0

                        radarDataArrayList.clear()

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

                        val radarDataSet = RadarDataSet(radarDataArrayList, "각가의 운동에 대한 총 운동량")
                        radarDataSet.color = Color.BLUE
                        radarDataSet.valueFormatter = CountValueFormatter()     // "10개" 형식으로 변환

                        val radarData = RadarData()
                        radarData.addDataSet(radarDataSet)
                        val radarLabels = arrayOf("벤치프레스", "스쿼트", "데드리프트", "인클라인 벤치프레스", "레그 익스텐션", "케이블 크로스오버")
                        val xAxis_radar = binding.radarChart.xAxis
                        xAxis_radar.valueFormatter = IndexAxisValueFormatter(radarLabels)
                        binding.radarChart.data = radarData

                        binding.radarChart.invalidate()

                    } //onDataChange

                    override fun onCancelled(error: DatabaseError) {} //onCancelled
                }) //addValueEventListener

            benchBtn.setOnClickListener {
                chartBench.setVisibility(View.VISIBLE)
                chartSquat.setVisibility(View.INVISIBLE)
                chartDead.setVisibility(View.INVISIBLE)
                chartIncline.setVisibility(View.INVISIBLE)
                chartLegex.setVisibility(View.INVISIBLE)
                chartCrossover.setVisibility(View.INVISIBLE)
                chartNameText.setText("벤치프레스")
            }
            squatBtn.setOnClickListener {
                chartBench.setVisibility(View.INVISIBLE)
                chartSquat.setVisibility(View.VISIBLE)
                chartDead.setVisibility(View.INVISIBLE)
                chartIncline.setVisibility(View.INVISIBLE)
                chartLegex.setVisibility(View.INVISIBLE)
                chartCrossover.setVisibility(View.INVISIBLE)
                chartNameText.setText("스쿼트")
            }
            deadBtn.setOnClickListener {
                chartBench.setVisibility(View.INVISIBLE)
                chartSquat.setVisibility(View.INVISIBLE)
                chartDead.setVisibility(View.VISIBLE)
                chartIncline.setVisibility(View.INVISIBLE)
                chartLegex.setVisibility(View.INVISIBLE)
                chartCrossover.setVisibility(View.INVISIBLE)
                chartNameText.setText("데드리프트")
            }
            inclineBtn.setOnClickListener {
                chartBench.setVisibility(View.INVISIBLE)
                chartSquat.setVisibility(View.INVISIBLE)
                chartDead.setVisibility(View.INVISIBLE)
                chartIncline.setVisibility(View.VISIBLE)
                chartLegex.setVisibility(View.INVISIBLE)
                chartCrossover.setVisibility(View.INVISIBLE)
                chartNameText.setText("인클라인 벤치프레스")
            }
            legexBtn.setOnClickListener {
                chartBench.setVisibility(View.INVISIBLE)
                chartSquat.setVisibility(View.INVISIBLE)
                chartDead.setVisibility(View.INVISIBLE)
                chartIncline.setVisibility(View.INVISIBLE)
                chartLegex.setVisibility(View.VISIBLE)
                chartCrossover.setVisibility(View.INVISIBLE)
                chartNameText.setText("레그 익스텐션")
            }
            crossoverBtn.setOnClickListener {
                chartBench.setVisibility(View.INVISIBLE)
                chartSquat.setVisibility(View.INVISIBLE)
                chartDead.setVisibility(View.INVISIBLE)
                chartIncline.setVisibility(View.INVISIBLE)
                chartLegex.setVisibility(View.INVISIBLE)
                chartCrossover.setVisibility(View.VISIBLE)
                chartNameText.setText("케이블 크로스오버")
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

        barDataSet.setColors(Color.GRAY)
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

        // 최대 x좌표 기준으로 몇개를 보여줄지 (최소값, 최대값)
        barChart.setVisibleXRange(1f, 10f)

        // x축 값 포맷
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labelsNames)

        // x축 라벨 네임 위치 지정
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        barChart.animateY(2000)
        barChart.invalidate()

        // y축 설정


        // y축 설정
        //var max_counts: Int = dateCountsDataArrayList.get(0).getCounts().toInt() // 최대 개수
        var countsList = ArrayList<Int>()

        //Log.d("최댓값 확인 : ", dateCountsDataArrayList.get(0).counts)
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
        return String.format(Locale.getDefault(), "%.0f개", value)
    }
}

class yAxisValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return String.format(Locale.getDefault(), "%.0f", value)
    }
}