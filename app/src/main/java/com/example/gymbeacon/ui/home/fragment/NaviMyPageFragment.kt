package com.example.gymbeacon.ui.home.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.FragmentNaviMypageBinding
import com.example.gymbeacon.ui.chart.ChartActivity
import com.example.gymbeacon.ui.common.CommonUtil
import com.example.gymbeacon.ui.common.HealthEntity
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class NaviMyPageFragment : Fragment() {
    lateinit var binding: FragmentNaviMypageBinding
    var returnCount = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_navi_mypage, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dateFormat: DateFormat = SimpleDateFormat("yyyy년 MM월 dd일")

        with(binding) {
            val date: Date = Date(calendarView.date)
            myPageDate.text = dateFormat.format(date)
            calendarView.setOnDateChangeListener { calendarView, year, month, dayOfMonth ->
                binding.myPageDate.text = "${year}년 ${month + 1}월 ${dayOfMonth}일"
                setData(year, month+1, dayOfMonth)
                binding.gymCurrentUserValueTextview.text =
                    returnCount.toString()
            }

            chartBtn.setOnClickListener {
                goToChartActivity()
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


        CommonUtil.myRef.orderByChild("uid").equalTo(CommonUtil.mAuth.uid)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val dataList = mutableListOf<HealthEntity>()

                    var result = snapshot.getValue<HealthEntity>()
                    if (result != null) {
                        if (result.timestamp == nowTimeStamp) {
                            sumCount.add(result.count!!.toInt())
                            returnCount = sumCount.sum()
                            Log.e("result", "${result},${nowTimeStamp},${sumCount}")
                            Log.e("result2", "sum of count: ${sumCount.sum()}")
                        }
                    }


                }


                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    TODO("Not yet implemented")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    fun goToChartActivity() {
        Intent(activity, ChartActivity::class.java).also { startActivity(it) }
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