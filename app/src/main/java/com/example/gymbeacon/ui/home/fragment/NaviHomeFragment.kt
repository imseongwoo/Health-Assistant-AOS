package com.example.gymbeacon.ui.home.fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.FragmentNaviHomeBinding
import com.example.gymbeacon.model.HealthEntity
import com.example.gymbeacon.ui.chart.DateCountsData
import com.example.gymbeacon.ui.common.CommonUtil
import com.example.gymbeacon.ui.home.LowerBodyCategoryActivity
import com.example.gymbeacon.ui.home.UpperBodyCategoryActivity
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class NaviHomeFragment : Fragment() {
    lateinit var binding: FragmentNaviHomeBinding
    var auth: FirebaseAuth? = null

    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var entityArrayList : ArrayList<HealthEntity>
    var recently_date: String? = "test"

    // 파이 차트 데이터
    val pieEntryArrayList = ArrayList<PieEntry>()
    lateinit var labelsNames_pie: ArrayList<String>
    lateinit var dateCountsDataArrayList_pie: ArrayList<DateCountsData>

    var counts_lower = 0
    var counts_back = 0
    var counts_chest = 0

    // 라인 차트 데이터 담을 리스트
//    // 가슴 운동
//    val chestDateList = mutableListOf<String>()
//    val chestCountList = mutableListOf<String>()
//    var chestCount: Int = 0
//
//    // 등 운동
//    val backDateList = mutableListOf<String>()
//    val backCountList = mutableListOf<String>()
//    var backCount: Int = 0
//
//    // 하체 운동
//    val lowerDateList = mutableListOf<String>()
//    val lowerCountList = mutableListOf<String>()
//    var lowerCount: Int = 0
//
//    val dateList = mutableListOf<String>()
//    val countList = mutableListOf<String>()
//
//    var chartData = LineData()
//
//    val chestEntry = ArrayList<Entry>()
//    val chestLabels = ArrayList<String>()
//    val lowerEntry = ArrayList<Entry>()
//    val lowerLabels = ArrayList<String>()
//    val backEntry = ArrayList<Entry>()
//    val backLabels = ArrayList<String>()
//
//    // 범례
//    lateinit var legend: Legend

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_navi_home,container,false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val userEmojiUnicode = 0x1F6B9
        // 사용자 최근 정보 불러오기
        auth = FirebaseAuth.getInstance()
        val user = auth?.currentUser
        user?.let {
            // Name, email address, and profile photo Url
            val name = it.displayName
            val email = it.email
            var email_split = email?.split("@")
            val user_id = email_split?.get(0)

            binding.textViewUserHello.text = "${String(Character.toChars(userEmojiUnicode))} " + user_id + "님 안녕하세요!"
            binding.textViewUserRecently.text = user_id + "님의 마지막 운동 기록"
            binding.textViewTotalExercise.text = user_id + "님의 전체 운동 기록"
        }

        entityArrayList = arrayListOf<HealthEntity>()

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.getReference("HealthEntity")

        CommonUtil.myRef.orderByChild("uid").equalTo(CommonUtil.mAuth.uid)
            .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                //dateCountsDataArrayList_pie.clear() // 파이 차트 리스트 초기화
                pieEntryArrayList.clear()
                entityArrayList.clear()
                //labelsNames_pie.clear()

                for(shot in snapshot.children) {
                    val uid = shot.child("uid").getValue(String::class.java)
                    val date = shot.child("timestamp").getValue(String::class.java)
                    val count = shot.child("count").getValue(String::class.java)
                    val exercise = shot.child("exercise").getValue(String::class.java)
                    val p = HealthEntity(uid, date, count, exercise)



                    if (p != null) {
                        entityArrayList.add(p)
                    }

//                    // 라인 차트에 쓸 데이터 담기
//                    if (date != null) {
//                        dateList.add(date)
//                    }
//
//                    if (count != null) {
//                        countList.add(count)
//                    }
                    // 하체
                    if (exercise == "스쿼트" || exercise == "레그 익스텐션") {
                        if (count != null) {
                            counts_lower += count.toInt()
                        }
                    }
                    // 가슴
                    if (exercise == "벤치프레스" || exercise == "인클라인 벤치프레스" || exercise == "케이블 크로스오버") {
                        if (count != null) {
                            counts_chest += count.toInt()
                        }
                    }
                    //등
                    if (exercise == "데드리프트") {
                        if (count != null) {
                            counts_back += count.toInt()
                        }
                    }

                    // 하체
//                    if (exercise == "스쿼트" || exercise == "레그 익스텐션") {
//                        if (count != null) {
//                            lowerCountList.add(count)
//                        }
//                        if (date != null) {
//                            lowerDateList.add(date)
//                        }
//                    }
//                    // 가슴
//                    if (exercise == "벤치프레스" || exercise == "인클라인 벤치프레스" || exercise == "케이블 크로스오버") {
//                        if (count != null) {
//                            chestCountList.add(count)
//                        }
//                        if (date != null) {
//                            chestDateList.add(date)
//                        }
//                    }
//                    //등
//                    if (exercise == "데드리프트") {
//                        if (count != null) {
//                            backCountList.add(count)
//                        }
//                        if (date != null) {
//                            backDateList.add(date)
//                        }
//                    }

                    //
                }

                var recently_index = 0
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                Log.d("날짜 포맷 : ",
                    LocalDate.parse(entityArrayList.get(1).timestamp, formatter).toString()
                )
                for(i in 0 until entityArrayList.size-1) {
                    //if (LocalDate.parse(dateArrayList.get(i), formatter) > LocalDate.parse(dateArrayList.get(i+1), formatter)) {
                    //    recently_date = dateArrayList.get(i)
                    //}
                    if (LocalDate.parse(entityArrayList.get(i).timestamp, formatter).isBefore(LocalDate.parse(entityArrayList.get(i+1).timestamp, formatter))) {
                        recently_date = entityArrayList.get(i+1).timestamp
                        recently_index = i+1
                    }
                }

                Log.d("날짜 비교 : ",
                    LocalDate.parse(entityArrayList.get(0).timestamp, formatter).isAfter(LocalDate.parse(entityArrayList.get(1).timestamp, formatter))
                        .toString()
                )
                entityArrayList.get(0).timestamp?.let { Log.d("최근 날짜2 : ", it) }
                Log.d("최근 날짜 : ", recently_date.toString())
                binding.textViewExerciseRecentlyDate2.text = recently_date
                binding.textViewExerciseRecentlyName2.text = entityArrayList.get(recently_index).exercise


                // 최근 운동 정보
                for(i in entityArrayList.size-1 downTo 0) {
                    val tableRow = TableRow(context)
                    tableRow?.layoutParams = TableRow.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    val textView_date = TextView(context)
                    val textView_name = TextView(context)
                    val textView_count = TextView(context)
                    textView_date?.text = entityArrayList.get(i).timestamp
                    textView_name?.text = entityArrayList.get(i).exercise
                    textView_count?.text = entityArrayList.get(i).count + "회"

                    textView_date.gravity = Gravity.CENTER
                    textView_date.textSize = 16f
                    textView_name.gravity = Gravity.CENTER
                    textView_name.textSize = 16f
                    textView_count.gravity = Gravity.CENTER
                    textView_count.textSize = 16f
                    textView_date.setBackgroundResource(R.drawable.table_inside)
                    textView_name.setBackgroundResource(R.drawable.table_inside)
                    textView_count.setBackgroundResource(R.drawable.table_inside)

                    textView_date.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1F)
                    textView_name.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1F)
                    textView_count.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1F)

                    tableRow?.addView(textView_date)
                    tableRow?.addView(textView_name)
                    tableRow?.addView(textView_count)

                    binding.tableExerciseInfo.addView(tableRow)
                }

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
                if (pieEntryArrayList != null) {
                    val pieChartColors =
                        ColorTemplate.COLORFUL_COLORS.copyOf(pieEntryArrayList.size).toList()
                    pieDataSet.colors = pieChartColors
                }

                val description = Description()
                description.text = ""
                description.textSize = 15f
                binding.pieChart.description = description

                val pieData = PieData(pieDataSet)
                pieDataSet.valueFormatter = PercentFormatter(binding.pieChart)

                binding.pieChart.data = pieData

                binding.pieChart.animateXY(2000, 2000)
                binding.pieChart.setUsePercentValues(true)


                // 라인 차트
//                var lineChart: LineChart = binding.lineChart
//
//                LineChartDesign(lineChart)
//
//                //val entries = ArrayList<Entry>()
//
//                setChartData(chestEntry, chestLabels, lineChart, "가슴", chartData)
//                setChartData(backEntry, backLabels, lineChart, "등", chartData)
//                setChartData(lowerEntry, lowerLabels, lineChart, "하체", chartData)
//
//                prepareChartData(chartData, lineChart)

//                for(i in 0 until countList.size){
//                    entries.add( Entry(i.toFloat(), countList[i].toFloat() ))
//                }
//
//                val labels = ArrayList<String>()
//                for(i in 0 until dateList.size) {
//                    labels.add(dateList[i])
//                }
//
//                val dataSet = LineDataSet(entries, "")
//
//                lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

                //클릭시 x축 label 출력
//                lineChart.setOnChartValueSelectedListener(object: OnChartValueSelectedListener {
//                    override fun onValueSelected(e: Entry, h: Highlight){
//                        val xAxisLabel = e.x.let{
//                            lineChart.xAxis.valueFormatter.getAxisLabel(it, lineChart.xAxis)
//                        }
//                        binding.test.text= xAxisLabel
//                    }
//                    override fun onNothingSelected() {
//                    }
//                })

//                lineChart.getTransformer(YAxis.AxisDependency.LEFT)
//                lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

//                val data = LineData(dataSet)
//
//                lineChart.data = data
//                lineChart.invalidate()

            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
//        Log.d("최근 날짜2 : ", dateArrayList.get(0))
//        Log.d("최근 날짜 : ", recently_date.toString())
//        binding.textViewExerciseRecentlyDate2.text = recently_date



        with(binding) {
            textViewUpperBody.setOnClickListener {
                goToUpperBodyCategoryActivity()
            }
            textViewLowerBody.setOnClickListener {
                goToLowerBodyCategoryActivity()
            }
        }

    }

    fun goToUpperBodyCategoryActivity() {
        Intent(activity, UpperBodyCategoryActivity::class.java).also { startActivity(it) }
    }

    fun goToLowerBodyCategoryActivity() {
        Intent(activity, LowerBodyCategoryActivity::class.java).also { startActivity(it) }
    }

    // 라인 차트 설정 함수
//    fun LineChartDesign(lineChart: LineChart) {
//        lineChart.setExtraBottomOffset(15f) // 간격
//        lineChart.description.isEnabled = false // chart 밑에 description 표시 유무
//
//        // Legend는 차트의 범례
//        legend = lineChart.legend
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
//
//        // XAxis (아래쪽) - 선 유무, 사이즈, 색상, 축 위치 설정
//        val xAxis = lineChart.xAxis
//        xAxis.setDrawAxisLine(false)
//        xAxis.setDrawGridLines(false)
//        xAxis.position = XAxis.XAxisPosition.BOTTOM // x축 데이터 표시 위치
//        xAxis.granularity = 1f
//        xAxis.textSize = 14f
//        xAxis.textColor = Color.rgb(118, 118, 118)
//        xAxis.spaceMin = 0.1f // Chart 맨 왼쪽 간격 띄우기
//        xAxis.spaceMax = 0.1f // Chart 맨 오른쪽 간격 띄우기
//
//        // YAxis(Right) (왼쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
//        val yAxisLeft = lineChart.axisLeft
//        yAxisLeft.textSize = 14f
//        yAxisLeft.textColor = Color.rgb(163, 163, 163)
//        yAxisLeft.setDrawAxisLine(false)
//        yAxisLeft.axisLineWidth = 2f
//        yAxisLeft.axisMinimum = 0f // 최솟값
//        //yAxisLeft.axisMaximum = RANGE[0][range].toFloat() // 최댓값
//        //yAxisLeft.granularity = RANGE[1][range].toFloat()
//
//        // YAxis(Left) (오른쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
//        val yAxis = lineChart.axisRight
//        yAxis.setDrawLabels(false) // label 삭제
//        yAxis.textColor = Color.rgb(163, 163, 163)
//        yAxis.setDrawAxisLine(false)
//        yAxis.axisLineWidth = 2f
//        yAxis.axisMinimum = 0f // 최솟값
//        //yAxis.axisMaximum = RANGE[0][range].toFloat() // 최댓값
//        //yAxis.granularity = RANGE[1][range].toFloat()
//
//        // XAxis에 원하는 String 설정하기 (날짜)
////        xAxis.valueFormatter = object : ValueFormatter() {
////
////            override fun getFormattedValue(value: Float): String {
////                return LABEL[range][value.toInt()]
////            }
////        }
//    }

//    fun setChartData(entry: ArrayList<Entry>, labels: ArrayList<String>, lineChart: LineChart, check_exercise: String, chartData: LineData) {
//
////      val chartData = LineData()
//
//        for(i in 0 until countList.size){
//            entry.add( Entry(i.toFloat(), countList[i].toFloat() ))
//        }
//
//        //val labels = ArrayList<String>()
//        for(i in 0 until dateList.size) {
//            labels.add(dateList[i])
//        }
//
//        //val dataSet = LineDataSet(entry, "")
//
//        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
//        lineChart.getTransformer(YAxis.AxisDependency.LEFT)
//        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
//
//        // 4개 앱의 DataSet 추가 및 선 커스텀
//
//        var lineDataSet: LineDataSet? = null
//
//        // line 데이터 운동 별 설정
//        if (check_exercise == "등"){
//            lineDataSet = LineDataSet(entry, "등 운동")
//            lineDataSet?.color = Color.rgb(255, 155, 155)
//            lineDataSet?.setCircleColor(Color.rgb(255, 155, 155))
//        }
//        if (check_exercise == "가슴"){
//            lineDataSet = LineDataSet(entry, "가슴 운동")
//            lineDataSet?.color = Color.rgb(178, 223, 138)
//            lineDataSet?.setCircleColor(Color.rgb(178, 223, 138))
//        }
//        if (check_exercise == "하체"){
//            lineDataSet = LineDataSet(entry, "하체 운동")
//            lineDataSet?.color = Color.rgb(166, 208, 227)
//            lineDataSet?.setCircleColor(Color.rgb(166, 208, 227))
//        }
//
//        //val lineDataSet = LineDataSet(entry, "")
//        chartData.addDataSet(lineDataSet)
//
//        lineDataSet?.lineWidth = 3f
//        lineDataSet?.circleRadius = 6f
//        lineDataSet?.setDrawValues(false)
//        lineDataSet?.setDrawCircleHole(true)
//        lineDataSet?.setDrawCircles(true)
//        lineDataSet?.setDrawHorizontalHighlightIndicator(false)
//        lineDataSet?.setDrawHighlightIndicators(false)
//        //lineDataSet?.color = Color.rgb(255, 155, 155)
////        lineDataSet?.color = Color.rgb(255, 155, 155)
////        lineDataSet?.setCircleColor(Color.rgb(255, 155, 155))
//
//        // 앱2
////        val lineDataSet2 = LineDataSet(entry2, APPS[1])
////        chartData.addDataSet(lineDataSet2)
////
////        lineDataSet2.lineWidth = 3f
////        lineDataSet2.circleRadius = 6f
////        lineDataSet2.setDrawValues(false)
////        lineDataSet2.setDrawCircleHole(true)
////        lineDataSet2.setDrawCircles(true)
////        lineDataSet2.setDrawHorizontalHighlightIndicator(false)
////        lineDataSet2.setDrawHighlightIndicators(false)
////        lineDataSet2.color = Color.rgb(178, 223, 138)
////        lineDataSet2.circleColor = Color.rgb(178, 223, 138)
////
////        // 앱3
////        val lineDataSet3 = LineDataSet(entry3, APPS[2])
////        chartData.addDataSet(lineDataSet3)
////
////        lineDataSet3.lineWidth = 3f
////        lineDataSet3.circleRadius = 6f
////        lineDataSet3.setDrawValues(false)
////        lineDataSet3.setDrawCircleHole(true)
////        lineDataSet3.setDrawCircles(true)
////        lineDataSet3.setDrawHorizontalHighlightIndicator(false)
////        lineDataSet3.setDrawHighlightIndicators(false)
////        lineDataSet3.color = Color.rgb(166, 208, 227)
////        lineDataSet3.circleColor = Color.rgb(166, 208, 227)
//
//        chartData.setValueTextSize(15F)
//        //return chartData
//    }

//    fun prepareChartData(data: LineData, lineChart: LineChart) {
//        lineChart.data = data // LineData 전달
//        lineChart.invalidate() // LineChart 갱신해 데이터 표시
//    }

    companion object {
        fun newInstance(): NaviHomeFragment {
            val args = Bundle().apply {
            }
            val fragment = NaviHomeFragment()
            fragment.arguments = args
            return fragment
        }
    }
}