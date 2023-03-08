package com.example.gymbeacon.ui.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.FragmentNaviMypageBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class NaviMyPageFragment: Fragment() {
    lateinit var binding: FragmentNaviMypageBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_navi_mypage,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dateFormat: DateFormat = SimpleDateFormat("yyyy년 MM월 dd일")

        with(binding) {
            val date: Date = Date(calendarView.date)
            myPageDate.text = dateFormat.format(date)
            calendarView.setOnDateChangeListener { calendarView, year, month, dayOfMonth ->
                binding.myPageDate.text = "${year}년 ${month+1}월 ${dayOfMonth}일"
            }
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