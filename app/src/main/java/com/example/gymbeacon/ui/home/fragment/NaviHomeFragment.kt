package com.example.gymbeacon.ui.home.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gymbeacon.R
import com.example.gymbeacon.ViewModelFactory
import com.example.gymbeacon.databinding.FragmentNaviHomeBinding
import com.example.domain.model.HealthEntity
import com.example.domain.auth.CommonUtil
import com.example.gymbeacon.ui.home.detail.DetailActivity
import com.example.gymbeacon.ui.home.viewmodel.NaviViewModel
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class NaviHomeFragment : Fragment() {
    private lateinit var binding: FragmentNaviHomeBinding
    private var auth: FirebaseAuth? = null
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private val entityArrayList = ArrayList<HealthEntity>()
    private var recentlyDate: String? = "test"
    private var maxDate: LocalDate? = null
    private val viewModel: NaviViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_navi_home, container, false)
        binding.vm = viewModel
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userEmojiUnicode = 0x1F464
        auth = FirebaseAuth.getInstance()
        val user = auth?.currentUser
        user?.let {
            val name = it.displayName
            val email = it.email
            val emailSplit = email?.split("@")
            val userId = emailSplit?.get(0)
            binding.textViewUserHello.text =
                "${String(Character.toChars(userEmojiUnicode))}$userId 님"
            binding.textViewUserRecently.text = "마지막 운동 기록"
        }

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.getReference("HealthEntity")
        subscribe()
        viewModel.getHomeWeightData()
/*
        CommonUtil.myRef.orderByChild("uid").equalTo(CommonUtil.mAuth.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pieEntryArrayList.clear()
                    entityArrayList.clear()
                    recentlyDate = null

                    for (shot in snapshot.children) {
                        val uid = shot.child("uid").getValue(String::class.java)
                        val date = shot.child("timestamp").getValue(String::class.java)
                        val count = shot.child("count").getValue(String::class.java)
                        val exercise = shot.child("exercise").getValue(String::class.java)
                        val p = HealthEntity(uid, date, count, exercise)

                        if (p != null) {
                            entityArrayList.add(p)
                        }

                    }

                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val recentlyExercises = mutableListOf<String>()

                    for (i in 0 until entityArrayList.size - 1) {
                        val currentDate = LocalDate.parse(entityArrayList[i].timestamp, formatter)
                        val nextDate = LocalDate.parse(entityArrayList[i + 1].timestamp, formatter)

                        if (currentDate.isBefore(nextDate) &&
                            (maxDate == null || nextDate.isAfter(maxDate)) &&
                            entityArrayList[i + 1].count?.toInt() != 0
                        ) {
                            maxDate = nextDate
                            recentlyExercises.clear()
                            entityArrayList[i + 1].exercise?.let { recentlyExercises.add(it) }
                        } else if (maxDate != null && nextDate.isEqual(maxDate) && entityArrayList[i + 1].count?.toInt() != 0) {
                            entityArrayList[i + 1].exercise?.let { recentlyExercises.add(it) }
                        }
                    }

                    entityArrayList.get(0).timestamp?.let { Log.d("최근 날짜2 : ", it) }
                    Log.d("최근 날짜 : ", recentlyDate.toString())
                    binding.textViewExerciseRecentlyDate2.text = maxDate.toString()
                    Log.d("최근 운동 명 불러오기 : ", recentlyExercises.get(0))

                    for (exercise in recentlyExercises) {
                        println(exercise)
                    }

                    maxDate.toString()?.let { viewModel.getDbData(it) }


                }

                override fun onCancelled(error: DatabaseError) {
                }
            })*/

        with(binding) {
            benchBox.setOnClickListener { goToDetailActivity("벤치프레스") }
            latpulldownBox.setOnClickListener { goToDetailActivity("랫 풀 다운") }
            inclineBox.setOnClickListener { goToDetailActivity("인클라인 벤치프레스") }
            legexBox.setOnClickListener { goToDetailActivity("레그 익스텐션") }
            squatBox.setOnClickListener { goToDetailActivity("스쿼트") }
            deadLiftBox.setOnClickListener { goToDetailActivity("데드리프트") }
        }

    }

    private fun goToDetailActivity(posValue: String) {
        val intent = Intent(requireActivity(), DetailActivity::class.java)
        intent.putExtra("upper", posValue)
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun subscribe() {
        with(viewModel) {
            pieChartData.observe(viewLifecycleOwner) {
                val pieEntryArrayList = ArrayList<PieEntry>()
                if (it.isSuccess) {
                    with(binding.pieChart) {
                        setUsePercentValues(true)
                        animateXY(1000, 1000)

                        if (it.countsBack != 0) {
                            pieEntryArrayList.add(PieEntry(it.countsBack.toFloat(), "등"))
                        }
                        if (it.countsLower != 0) {
                            pieEntryArrayList.add(PieEntry(it.countsLower.toFloat(), "하체"))
                        }
                        if (it.countsUpper != 0) {
                            pieEntryArrayList.add(PieEntry(it.countsUpper.toFloat(), "가슴"))
                        }

                        val pieDataSet = PieDataSet(pieEntryArrayList, "")
                        val colorsPie = listOf(
                            Color.rgb(110, 87, 233),
                            Color.rgb(2, 204, 204),
                            Color.rgb(233, 93, 132)
                        )

                        pieDataSet.apply {
                            colors = colorsPie
                            valueTextSize = 14f
                            valueTextColor = Color.BLACK
                            sliceSpace = 5f
                        }
                        val pieData = PieData(pieDataSet)
                        pieDataSet.valueFormatter = PercentFormatter(binding.pieChart)

                        val des = Description()
                        des.text = ""
                        description = des

                        data = pieData

                    }
                }
            }
        }

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
            val exerciseList = listOf(
                "벤치프레스", "인클라인 벤치프레스", "랫 풀 다운",
                "스쿼트", "데드리프트", "레그 익스텐션"
            )
            val layoutIds = listOf(
                R.id.text_view_exercise_info1, R.id.text_view_exercise_info2,
                R.id.text_view_exercise_info3, R.id.text_view_exercise_info4,
                R.id.text_view_exercise_info5, R.id.text_view_exercise_info6
            )

            for (exercise in exerciseList) {
                val countPair = exerciseCountMap[exercise]
                val num = countPair?.second ?: 0
                val exerciseSetPair = Pair(exercise, num)
                exerciseSet.add(exerciseSetPair)
            }

            for (i in exerciseSet.indices) {
                val exercise = if (i < exerciseSet.size) exerciseSet[i].first else exerciseList[i]
                val num = if (i < exerciseSet.size) exerciseSet[i].second else 0
                val exerciseTextView = view?.findViewById<TextView>(layoutIds[i])
                exerciseTextView!!.text = "· $exercise : ${num.toString()}세트"
            }
        }
    }

    companion object {
        fun newInstance(): NaviHomeFragment {
            val args = Bundle().apply {}
            val fragment = NaviHomeFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
