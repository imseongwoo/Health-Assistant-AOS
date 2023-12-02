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
import com.example.gymbeacon.databinding.FragmentNaviHomeBinding
import com.example.domain.model.HealthEntity
import com.example.gymbeacon.ui.home.detail.DetailActivity
import com.example.gymbeacon.ui.home.viewmodel.NaviViewModel
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class NaviHomeFragment : Fragment() {
    private lateinit var binding: FragmentNaviHomeBinding
    private var auth: FirebaseAuth? = null
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
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
                with(binding.pieChart) {
                    setUsePercentValues(true)
                    animateXY(1000, 1000)

                    val pieDataSet = PieDataSet(it, "")
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

        viewModel.recentWeightData.observe(viewLifecycleOwner) {
            with(binding) {
                textViewExerciseRecentlyDateSecond.text = it.recentDate
                textViewExerciseInfo.text = "벤치프레스 : ${it.countBench}회"
                textViewExerciseInfoSecond.text = "인클라인 벤치프레스 : ${it.countInclineBench}회"
                textViewExerciseInfoThird.text = "랫 풀 다운 : ${it.countLatPullDown}회"
                textViewExerciseInfoFourth.text = "스쿼트 : ${it.countSquat}회"
                textViewExerciseInfoFifth.text = "데드리프트 : ${it.countDeadLift}회"
                textViewExerciseInfoSixth.text = "레그 익스텐션 : ${it.countLegExtenstion}회"
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
