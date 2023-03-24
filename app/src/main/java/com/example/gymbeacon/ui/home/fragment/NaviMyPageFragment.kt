package com.example.gymbeacon.ui.home.fragment

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
import com.example.gymbeacon.ui.home.adapter.MyPageViewPagerAdapter
import com.example.gymbeacon.ui.home.viewmodel.NaviMyPageViewModel
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class NaviMyPageFragment : Fragment() {
    lateinit var binding: FragmentNaviMypageBinding
    lateinit var viewPager: ViewPager2
    lateinit var viewPagerExerciseCountMap: MutableMap<String, Pair<Int,Int>>
    var returnCount = 1
    private val viewModel: NaviMyPageViewModel by viewModels { ViewModelFactory() }

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