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

                pieEntryArrayList.clear()
                entityArrayList.clear()

                for(shot in snapshot.children) {
                    val uid = shot.child("uid").getValue(String::class.java)
                    val date = shot.child("timestamp").getValue(String::class.java)
                    val count = shot.child("count").getValue(String::class.java)
                    val exercise = shot.child("exercise").getValue(String::class.java)
                    val p = HealthEntity(uid, date, count, exercise)



                    if (p != null) {
                        entityArrayList.add(p)
                    }

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

                }

                var recently_index = 0
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                Log.d("날짜 포맷 : ",
                    LocalDate.parse(entityArrayList.get(1).timestamp, formatter).toString()
                )
                for(i in 0 until entityArrayList.size-1) {
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

            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


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