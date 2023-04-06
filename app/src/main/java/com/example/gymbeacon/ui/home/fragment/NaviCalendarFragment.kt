package com.example.gymbeacon.ui.home.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.gymbeacon.R
import com.example.gymbeacon.ViewModelFactory
import com.example.gymbeacon.databinding.FragmentNaviCalendarBinding
import com.example.gymbeacon.ui.home.adapter.MyPageViewPagerAdapter
import com.example.gymbeacon.ui.home.viewmodel.NaviMyPageViewModel
import java.io.File

class NaviCalendarFragment : Fragment() {
    lateinit var binding: FragmentNaviCalendarBinding
    lateinit var viewPager: ViewPager2
    lateinit var viewPagerExerciseCountMap: MutableMap<String, Pair<Int, Int>>
    private val viewModel: NaviMyPageViewModel by viewModels { ViewModelFactory() }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Log.d("NaviMyPage", "READ_EXTERNAL_STORAGE permission denied")
                checkPermission()
            }
        }

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

        checkPermission()

        with(binding) {
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

                    this@NaviCalendarFragment.viewPager = binding.viewPager
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
        val customDay = String.format("%02d", dayOfMonth)
        val dateStrForVideo = "${year}${customMonth}${customDay}"

        val folderPath = "${Environment.getExternalStorageDirectory()}/DCIM/Koreatech"
        val folder = File(folderPath)


        val videoFiles = folder.listFiles { file ->
            file.name.startsWith(dateStrForVideo) && file.extension == "mp4"
        }
        if (videoFiles == null || videoFiles.isEmpty()) {
            Log.d("NaviMyPage", "No video files found for $dateStrForVideo")
        } else {
            for (file in videoFiles) {
                Log.d("NaviMyPage", "Video file: ${file.name}")
            }

        }

        viewModel.getDbData(nowTimeStamp)
    }

    fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            Log.e("NaviMyPage", "권한 허용됨")
        }
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