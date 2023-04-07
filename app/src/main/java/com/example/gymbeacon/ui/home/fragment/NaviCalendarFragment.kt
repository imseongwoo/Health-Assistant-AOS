package com.example.gymbeacon.ui.home.fragment

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
import com.example.gymbeacon.databinding.FragmentNaviCalendarBinding
import com.example.gymbeacon.ui.home.adapter.MyPageViewPagerAdapter
import com.example.gymbeacon.ui.home.viewmodel.NaviMyPageViewModel

class NaviCalendarFragment : Fragment() {
    lateinit var binding: FragmentNaviCalendarBinding
    lateinit var viewPager: ViewPager2
    lateinit var viewPagerExerciseCountMap: MutableMap<String, Pair<Int, Int>>
    private val viewModel: NaviMyPageViewModel by viewModels { ViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_navi_calendar, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            calendarView.setOnDateChangeListener { calendarView, year, month, dayOfMonth ->
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

                    this@NaviCalendarFragment.viewPager = binding.viewPager
                    viewPager.adapter = MyPageViewPagerAdapter(exerciseCountMap)
                    viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

                }


            }
        }
    }

    fun setData(year: Int, month: Int, dayOfMonth: Int) {
        val customMonth = String.format("%02d", month)
        val customDay = String.format("%02d", dayOfMonth)
        val nowTimeStamp = year.toString() + "-" + customMonth + "-" + customDay
        viewModel.getDbData(nowTimeStamp)
    }

    companion object {
        fun newInstance(): NaviCalendarFragment {
            val args = Bundle().apply {
            }
            val fragment = NaviCalendarFragment()
            fragment.arguments = args
            return fragment
        }
    }
}