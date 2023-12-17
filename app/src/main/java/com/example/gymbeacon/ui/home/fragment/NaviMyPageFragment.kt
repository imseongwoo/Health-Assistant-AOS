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
import com.example.gymbeacon.ui.chart.MyMarkerView
import com.example.domain.auth.CommonUtil
import com.example.gymbeacon.ui.home.viewmodel.NaviViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class NaviMyPageFragment : Fragment() {
    lateinit var binding: FragmentNaviMypageBinding
    lateinit var viewPager: ViewPager2
    lateinit var viewPagerExerciseCountMap: MutableMap<String, Pair<Int,Int>>
    var returnCount = 1
    private val viewModel: NaviViewModel by viewModels { ViewModelFactory() }

    // 레이더차트에 필요한 운동별 count 사이즈
    var counts_bench = 0
    var counts_squat = 0
    var counts_dead = 0
    var counts_incline = 0
    var counts_legex = 0
    var counts_latpulldown = 0

    var radarDataArrayList = ArrayList<RadarEntry>()
    //val radarDataArrayList2 = ArrayList<RadarEntry>()

    // 파이 차트 데이터
    val pieEntryArrayList = ArrayList<PieEntry>()
    var counts_lower = 0
    var counts_back = 0
    var counts_chest = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_navi_mypage, container, false)


        binding.totalChartBtn.isSelected = true
        binding.totalChartBtn.setOnClickListener {
            // 이미 Fragment total Chart를 표시하고 있으므로 추가 작업이 필요하지 않음
            binding.totalChartBtn.isSelected = true
        }

        // partBtn 클릭 이벤트 리스너 설정
        binding.partChartBtn.setOnClickListener {
            binding.totalChartBtn.isSelected = false
            binding.partChartBtn.isSelected = true

            // partBtn 클릭 시 Fragment part chart로 전환
            val fragmentPart = PartChartFragment()
            val fragmentTransaction = parentFragmentManager.beginTransaction()

            fragmentTransaction.replace(R.id.fragmentContainer, fragmentPart)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()

        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dateFormat: DateFormat = SimpleDateFormat("yyyy년 MM월 dd일")

        with(binding) {
        //  실시간 DB 참조 위치(health/momentum) 설정
            CommonUtil.myRef.orderByChild("uid").equalTo(CommonUtil.mAuth.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {


                        // 레이더 차트 arrayList
                        counts_bench = 0
                        counts_squat = 0
                        counts_dead = 0
                        counts_incline = 0
                        counts_legex = 0
                        counts_latpulldown = 0

                        radarDataArrayList.clear()

                        // 파이 차트 arrayList
                        pieEntryArrayList.clear()
                        counts_back = 0
                        counts_lower = 0
                        counts_chest = 0


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
                                //dateCountsDataArrayList_bench.add(DateCountsData(date, counts))

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
                                //dateCountsDataArrayList_squat.add(DateCountsData(date, counts))

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
                                //dateCountsDataArrayList_dead.add(DateCountsData(date, counts))

                                counts_dead += counts!!.toInt()
                                counts_lower += counts!!.toInt()
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
                                //DateCountsDataArrayList_incline.add(DateCountsData(date, counts))

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
                                //dateCountsDataArrayList_legex.add(DateCountsData(date, counts))

                                counts_legex += counts!!.toInt()
                                counts_lower += counts!!.toInt()
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
                                //dateCountsDataArrayList_crossover.add(DateCountsData(date, counts))

                                counts_latpulldown += counts!!.toInt()
                                counts_back += counts!!.toInt()
                            }

                        }

                        // 레이더 차트
                        radarDataArrayList.add(RadarEntry(counts_bench.toFloat()))
                        radarDataArrayList.add(RadarEntry(counts_latpulldown.toFloat()))
                        radarDataArrayList.add(RadarEntry(counts_squat.toFloat()))
                        radarDataArrayList.add(RadarEntry(counts_dead.toFloat()))
                        radarDataArrayList.add(RadarEntry(counts_incline.toFloat()))
                        radarDataArrayList.add(RadarEntry(counts_legex.toFloat()))


                        val radarDataSet = RadarDataSet(radarDataArrayList, "운동 횟수")
                        radarDataSet.color = Color.rgb(187, 233, 213)
                        radarDataSet.lineWidth = 2f
                        radarDataSet.setDrawFilled(true)
                        radarDataSet.fillColor = Color.rgb(187, 233, 213)
                        radarDataSet.valueFormatter = CountValueFormatter()     // "10개" 형식으로 변환

                        val radarData = RadarData()
                        radarData.addDataSet(radarDataSet)
                        val radarLabels = arrayOf("벤치프레스", "랫 풀 다운", "스쿼트", "데드리프트", "인클라인 벤치프레스", "레그 익스텐션")

                        val xAxis_radar = binding.radarChart.xAxis
                        xAxis_radar.valueFormatter = IndexAxisValueFormatter(radarLabels)
                        binding.radarChart.data = radarData

                        val yAxis_radar = binding.radarChart.yAxis
                        yAxis_radar.axisMinimum = 0f

                        val legend_radar = radarChart.legend
                        legend_radar.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                        legend_radar.form = Legend.LegendForm.CIRCLE

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

                        val pieDataSet = PieDataSet(pieEntryArrayList, "")
                        pieDataSet.valueTextSize = 18f

                        // 색상 추가하기
                        val colors_pie = listOf(
                            Color.rgb(111, 87, 233),  // 등 영역의 색상
                            Color.rgb(2, 204, 204),  // 하체 영역의 색상
                            Color.rgb(233, 93, 132)   // 가슴 영역의 색상
                        )
                        pieDataSet.apply {
                            colors = colors_pie
                            valueTextColor = Color.BLACK
                        }

                        val description_pie = Description()
                        description_pie.text = ""
                        description_pie.textSize = 15f
                        binding.pieChart.description = description_pie

                        val pieData = PieData(pieDataSet)
                        pieDataSet.valueFormatter = PercentFormatter(binding.pieChart)
                        pieDataSet.valueTextSize = 18f

                        val legend_pie = pieChart.legend
                        legend_pie.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                        legend_radar.form = Legend.LegendForm.CIRCLE

                        binding.pieChart.data = pieData

                        binding.pieChart.animateXY(1000, 1000)
                        binding.pieChart.setUsePercentValues(true)
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })

        }

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