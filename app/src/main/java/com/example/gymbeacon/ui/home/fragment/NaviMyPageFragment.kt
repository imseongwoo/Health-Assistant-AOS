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
import com.example.gymbeacon.ui.home.adapter.MyPageViewPagerAdapter
import com.example.gymbeacon.ui.home.viewmodel.NaviMyPageViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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
        setChart()
        val dateFormat: DateFormat = SimpleDateFormat("yyyy년 MM월 dd일")


        with(binding) {
            val date: Date = Date(calendarView.date)
            myPageDate.text = dateFormat.format(date)
            calendarView.setOnDateChangeListener { calendarView, year, month, dayOfMonth ->
//                binding.myPageDate.text = "${year}년 ${month + 1}월 ${dayOfMonth}일"
                setData(year, month + 1, dayOfMonth)

                viewModel.dbData.observe(viewLifecycleOwner) { healthEntities ->
                    val exerciseCountMap = mutableMapOf<String, Pair<Int, Int>>()

                    for (healthEntity in healthEntities) {
                        val exercise = healthEntity.exercise
                        val count = healthEntity.count?.toIntOrNull() ?: 0

                        if (exercise != null && exercise.isNotEmpty()) {
                            val (sum, num) = exerciseCountMap.getOrDefault(exercise, Pair(0, 0))
                            exerciseCountMap[exercise] = Pair(sum + count, num + 1)
                        }
                    }

                    for ((exercise, countPair) in exerciseCountMap) {
                        val (sum, num) = countPair
                        val average = if (num > 0) sum / num else 0
                        Log.d("NaviMyPage", "$exercise: total=$sum, count=$num, average=$average")
                    }

                    viewPagerExerciseCountMap = exerciseCountMap

                    this@NaviMyPageFragment.viewPager = binding.viewPager
                    viewPager.adapter = MyPageViewPagerAdapter(exerciseCountMap)
                    viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

                }


            }

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
            }
            squatBtn.setOnClickListener {
                chartBench.setVisibility(View.INVISIBLE)
                chartSquat.setVisibility(View.VISIBLE)
                chartDead.setVisibility(View.INVISIBLE)
                chartIncline.setVisibility(View.INVISIBLE)
                chartLegex.setVisibility(View.INVISIBLE)
                chartCrossover.setVisibility(View.INVISIBLE)
            }
            deadBtn.setOnClickListener {
                chartBench.setVisibility(View.INVISIBLE)
                chartSquat.setVisibility(View.INVISIBLE)
                chartDead.setVisibility(View.VISIBLE)
                chartIncline.setVisibility(View.INVISIBLE)
                chartLegex.setVisibility(View.INVISIBLE)
                chartCrossover.setVisibility(View.INVISIBLE)
            }
            inclineBtn.setOnClickListener {
                chartBench.setVisibility(View.INVISIBLE)
                chartSquat.setVisibility(View.INVISIBLE)
                chartDead.setVisibility(View.INVISIBLE)
                chartIncline.setVisibility(View.VISIBLE)
                chartLegex.setVisibility(View.INVISIBLE)
                chartCrossover.setVisibility(View.INVISIBLE)
            }
            legexBtn.setOnClickListener {
                chartBench.setVisibility(View.INVISIBLE)
                chartSquat.setVisibility(View.INVISIBLE)
                chartDead.setVisibility(View.INVISIBLE)
                chartIncline.setVisibility(View.INVISIBLE)
                chartLegex.setVisibility(View.VISIBLE)
                chartCrossover.setVisibility(View.INVISIBLE)
            }
            crossoverBtn.setOnClickListener {
                chartBench.setVisibility(View.INVISIBLE)
                chartSquat.setVisibility(View.INVISIBLE)
                chartDead.setVisibility(View.INVISIBLE)
                chartIncline.setVisibility(View.INVISIBLE)
                chartLegex.setVisibility(View.INVISIBLE)
                chartCrossover.setVisibility(View.VISIBLE)
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

    fun setChart() {
        val entries = listOf(
            BarEntry(0f, 30f),
            BarEntry(1f, 80f),
            BarEntry(2f, 60f),
            BarEntry(3f, 50f),
            BarEntry(4f, 70f)
        )

        val dataSet = BarDataSet(entries, "Label")
        dataSet.color = Color.BLUE

        val data = BarData(dataSet)
        data.barWidth = 0.5f

        with(binding) {
            chart.data = data
            chart.setFitBars(true)
            chart.description.isEnabled = false
            chart.setDrawGridBackground(false)
            chart.animateY(1000)
        }

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
        barDataSet.setColors(*ColorTemplate.COLORFUL_COLORS)
        val description = Description()
        description.text = "날짜"
        barChart.description = description
        val barData = BarData(barDataSet)
        barChart.data = barData
        barChart.let { graphInitSetting(it, labelsNames) } // 차트 기본 세팅

        // 가장 최근에 추가한 데이터의 위치로 이동처리
        barChart.moveViewToX(barDataSet.entryCount.toFloat())
    }

    fun graphInitSetting(barChart: BarChart, labelsNames: ArrayList<String>) {

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
        barChart.setVisibleXRange(1f, 7f)

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
        val yAxis = barChart.axisLeft
        barChart.axisRight.isEnabled = false
        yAxis.axisMinimum = 0f
        yAxis.spaceMax = 1f
        yAxis.spaceMin = 1f
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