package com.example.gymbeacon.ui.home.fragment

import android.Manifest
import android.content.Intent
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.gymbeacon.R
import com.example.gymbeacon.ViewModelFactory
import com.example.gymbeacon.databinding.FragmentNaviCalendarBinding
import com.example.gymbeacon.model.Video
import com.example.gymbeacon.ui.common.VideoPlayerActivity
import com.example.gymbeacon.ui.home.adapter.VideoAdapter
import com.example.gymbeacon.ui.home.viewmodel.NaviMyPageViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class NaviCalendarFragment : Fragment() {
    lateinit var binding: FragmentNaviCalendarBinding
    lateinit var viewPager: ViewPager2
    lateinit var viewPagerExerciseCountMap: MutableMap<String, Pair<Int, Int>>
    private val viewModel: NaviMyPageViewModel by viewModels { ViewModelFactory() }
    private lateinit var videoAdapter: VideoAdapter

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

        val currentDate = Calendar.getInstance().time
        val formatter = SimpleDateFormat("MM.dd", Locale.getDefault())
        val currentDateFormatted = formatter.format(currentDate)

        binding.textViewExerciseVideo.text = "$currentDateFormatted 운동"

        with(binding) {
            calendarView.setOnDateChangeListener { calendarView, year, month, dayOfMonth ->
                setData(year, month + 1, dayOfMonth)
                textViewExerciseVideo.setText("${month+1}.${dayOfMonth} 운동")
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

//                    this@NaviCalendarFragment.viewPager = binding.viewPager
//                    viewPager.adapter = MyPageViewPagerAdapter(exerciseCountMap)
//                    viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

                }


            }
        }
    }

    fun setData(year: Int, month: Int, dayOfMonth: Int) {
        val customMonth = String.format("%02d", month)
        val customDay = String.format("%02d", dayOfMonth)
        val nowTimeStamp = year.toString() + "-" + customMonth + "-" + customDay

        val dateStrForVideo = "${year}${customMonth}${customDay}"
        val folderPath = "${Environment.getExternalStorageDirectory()}/DCIM/Koreatech"
        val folder = File(folderPath)

        val videoFiles = folder.listFiles { file ->
            file.name.startsWith(dateStrForVideo) && file.extension == "mp4"
        }
        // 저장된 동영상이 있을 경우와 없을 경우 처리
        if (videoFiles == null || videoFiles.isEmpty()) {
            Log.d("NaviMyPage", "No video files found for $dateStrForVideo")
            videoAdapter = VideoAdapter(emptyList()) {

            }

            binding.recyclerViewVideo.layoutManager =LinearLayoutManager(requireContext())
            binding.recyclerViewVideo.adapter = videoAdapter

            videoAdapter.notifyDataSetChanged()
        } else {    // 동영상이 있을 경우 동영상의 이름과 저장 경로를 Video data class에 저장 후 adapter로 넘겨줌
            val videos = videoFiles.map { file ->
                val exerciseName = file.name.substringBeforeLast(".mp4").substringAfterLast("_")
                Video(exerciseName, file.path)
            }

            videoAdapter = VideoAdapter(videos){
                val video = videos[it]
                val intent = Intent(requireContext(),VideoPlayerActivity::class.java)
                intent.putExtra("video_path", video.path)
                startActivity(intent)
            }


            binding.recyclerViewVideo.layoutManager =LinearLayoutManager(requireContext())
            binding.recyclerViewVideo.adapter = videoAdapter

            videoAdapter.notifyDataSetChanged()
            for (file in videoFiles) {
                Log.d("NaviMyPage", "Video file: ${file.name}, file path: ${file.path}")
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