package com.example.gymbeacon.ui.home.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
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
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.gymbeacon.R
import com.example.gymbeacon.ViewModelFactory
import com.example.gymbeacon.databinding.FragmentNaviHomeBinding
import com.example.gymbeacon.model.HealthEntity
import com.example.gymbeacon.model.Video
import com.example.gymbeacon.ui.chart.DateCountsData
import com.example.gymbeacon.ui.common.CommonUtil
import com.example.gymbeacon.ui.common.VideoPlayerActivity
import com.example.gymbeacon.ui.home.LowerBodyCategoryActivity
import com.example.gymbeacon.ui.home.UpperBodyCategoryActivity
import com.example.gymbeacon.ui.home.adapter.MyPageViewPagerAdapter
import com.example.gymbeacon.ui.home.adapter.VideoAdapter
import com.example.gymbeacon.ui.home.detail.DetailActivity
import com.example.gymbeacon.ui.home.viewmodel.NaviMyPageViewModel
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.ColorTemplate.COLORFUL_COLORS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.File
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
    // 최근 날짜를 찾기 위한 변수
    var maxDate: LocalDate? = null

    // 홈 화면 세트, 횟수, 평균 횟수 등
    private val viewModel: NaviMyPageViewModel by viewModels { ViewModelFactory() }
    lateinit var viewPagerExerciseCountMap: MutableMap<String, Pair<Int, Int>>
    lateinit var viewPager2: ViewPager2

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


        val userEmojiUnicode = 0x1F464
        // 사용자 최근 정보 불러오기
        auth = FirebaseAuth.getInstance()
        val user = auth?.currentUser
        user?.let {
            // Name, email address, and profile photo Url
            val name = it.displayName
            val email = it.email
            var email_split = email?.split("@")
            val user_id = email_split?.get(0)

            binding.textViewUserHello.text = "${String(Character.toChars(userEmojiUnicode))}" + user_id + "님"
            binding.textViewUserRecently.text = "마지막 운동 기록"

        }

        entityArrayList = arrayListOf<HealthEntity>()

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.getReference("HealthEntity")

        CommonUtil.myRef.orderByChild("uid").equalTo(CommonUtil.mAuth.uid)
            .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                pieEntryArrayList.clear()
                entityArrayList.clear()

                recently_date = null

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
                    if (exercise == "스쿼트" || exercise == "레그 익스텐션" || exercise == "데드리프트") {
                        if (count != null) {
                            counts_lower += count.toInt()
                        }
                    }
                    // 가슴
                    if (exercise == "벤치프레스" || exercise == "인클라인 벤치프레스") {
                        if (count != null) {
                            counts_chest += count.toInt()
                        }
                    }
                    //등
                    if (exercise == "랫 풀 다운") {
                        if (count != null) {
                            counts_back += count.toInt()
                        }
                    }

                }

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                Log.d("날짜 포맷 : ",
                    LocalDate.parse(entityArrayList.get(1).timestamp, formatter).toString()
                )

                val recently_exercises = mutableListOf<String>()    // 최근 운동 날짜에 대한 운동명들

                for(i in 0 until entityArrayList.size-1) {
                    val currentDate = LocalDate.parse(entityArrayList[i].timestamp, formatter)
                    val nextDate = LocalDate.parse(entityArrayList[i+1].timestamp, formatter)

                    // 현재 날짜와 다음 날짜를 비교하여 최대 날짜를 갱신
                    if (currentDate.isBefore(nextDate) && (maxDate == null || nextDate.isAfter(maxDate)) && entityArrayList[i+1].count?.toInt() != 0 ) {
                        maxDate = nextDate
                        recently_exercises.clear() // 이전에 저장된 exercise들을 모두 제거
                        entityArrayList[i+1].exercise?.let { recently_exercises.add(it) } // 새로운 exercise 추가
                    } else if (maxDate != null && nextDate.isEqual(maxDate) && entityArrayList[i+1].count?.toInt() != 0) {
                        entityArrayList[i+1].exercise?.let { recently_exercises.add(it) } // 같은 날짜의 exercise 추가
                    }
//                    if (LocalDate.parse(entityArrayList.get(i).timestamp, formatter).isBefore(LocalDate.parse(entityArrayList.get(i+1).timestamp, formatter))) {
//                        recently_date = entityArrayList.get(i+1).timestamp
//                    }
                }

                Log.d("날짜 비교 : ",
                    LocalDate.parse(entityArrayList.get(0).timestamp, formatter).isAfter(LocalDate.parse(entityArrayList.get(1).timestamp, formatter))
                        .toString()
                )
                entityArrayList.get(0).timestamp?.let { Log.d("최근 날짜2 : ", it) }
                Log.d("최근 날짜 : ", recently_date.toString())
                binding.textViewExerciseRecentlyDate2.text = maxDate.toString()
                Log.d("최근 운동 명 불러오기 : ", recently_exercises.get(0))
                // 최근 날짜에 해당하는 exercise들 출력
                for (exercise in recently_exercises) {
                    println(exercise)
                }
                //binding.textViewExerciseRecentlyName2.text = entityArrayList.get(recently_index).exercise


                // 최근 운동 이름과 횟수, 세트, 평균 횟수 나타내기
                maxDate.toString()?.let { viewModel.getDbData(it) }

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


                    val exerciseSet = ArrayList<Pair<String, Int>>()
                    val exerciseList = listOf("벤치프레스", "인클라인 벤치프레스", "랫 풀 다운", "스쿼트", "데드리프트", "레그 익스텐션")
                    val layoutIds = listOf(R.id.text_view_exercise_info1, R.id.text_view_exercise_info2, R.id.text_view_exercise_info3,
                        R.id.text_view_exercise_info4, R.id.text_view_exercise_info5, R.id.text_view_exercise_info6)

                    for (exercise in exerciseList) {
                        val countPair = exerciseCountMap[exercise]
                        val num = countPair?.second ?: 0 // 해당 운동의 num 값 또는 0을 가져옴

                        val exerciseSetPair = Pair(exercise, num)
                        exerciseSet.add(exerciseSetPair)
                    }

                    // 텍스트뷰에 exercise와 num 값을 넣어주는 작업
                    for (i in exerciseSet.indices) {
                        val exercise = if (i < exerciseSet.size) exerciseSet[i].first else exerciseList[i]
                        val num = if (i < exerciseSet.size) exerciseSet[i].second else 0

                        val exerciseTextView = view.findViewById<TextView>(layoutIds[i])
                        if (num != 0) {
                            exerciseTextView.text = " " + exercise + " : " + num.toString() + "세트"
                        } else {
                            exerciseTextView.text = "· " + exercise + " : -"
                        }

                    }

                    //viewPagerExerciseCountMap = exerciseCountMap

//                    this@NaviHomeFragment.viewPager2 = binding.viewPager
//                    viewPager2.adapter = MyPageViewPagerAdapter(exerciseCountMap)
//                    viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL

                }
                // 최근 운동 정보


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

                // 색상 추가하기
                val colors_pie = listOf(
                    Color.rgb(111, 87, 233),  // 등 영역의 색상
                    Color.rgb(2, 204, 204),  // 하체 영역의 색상
                    Color.rgb(233, 93, 132)   // 가슴 영역의 색상
                )

                pieDataSet.apply {
                    colors = colors_pie
                    valueTextSize = 14f
                    valueTextColor = Color.BLACK
                    sliceSpace = 5f
                }

                val legend_pie = binding.pieChart.legend
                legend_pie.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                legend_pie.form = Legend.LegendForm.CIRCLE
                //legend_pie.orientation = Legend.LegendOrientation.VERTICAL

                val description = Description()
                description.text = ""
                description.textSize = 15f
                binding.pieChart.description = description

                val pieData = PieData(pieDataSet)
                pieDataSet.valueFormatter = PercentFormatter(binding.pieChart)

                binding.pieChart.data = pieData

                binding.pieChart.animateXY(1000, 1000)
                binding.pieChart.setUsePercentValues(true)

            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


//        with(binding) {
//            textViewUpperBody.setOnClickListener {
//                goToUpperBodyCategoryActivity()
//            }
//            textViewLowerBody.setOnClickListener {
//                goToLowerBodyCategoryActivity()
//            }
//        }
        with(binding) {
            benchBox.setOnClickListener {
                goToDetailActivity("벤치프레스")
            }
            latpulldownBox.setOnClickListener {
                goToDetailActivity("랫 풀 다운")
            }
            inclineBox.setOnClickListener {
                goToDetailActivity("인클라인 벤치프레스")
            }
            legexBox.setOnClickListener {
                goToDetailActivity("레그 익스텐션")
            }
            squatBox.setOnClickListener {
                goToDetailActivity("스쿼트")
            }
            deadLiftBox.setOnClickListener {
                goToDetailActivity("데드리프트")
            }
        }

    }

    fun goToUpperBodyCategoryActivity() {
        Intent(activity, UpperBodyCategoryActivity::class.java).also { startActivity(it) }
    }

    fun goToLowerBodyCategoryActivity() {
        Intent(activity, LowerBodyCategoryActivity::class.java).also { startActivity(it) }
    }

    fun goToDetailActivity(posValue: String) {
        val intent = Intent(requireActivity(), DetailActivity::class.java)
        intent.putExtra("upper",posValue)
        startActivity(intent)
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