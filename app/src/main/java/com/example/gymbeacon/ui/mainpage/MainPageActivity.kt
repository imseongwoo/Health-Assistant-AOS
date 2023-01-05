package com.example.gymbeacon.ui.mainpage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.ActivityMainPageBinding

class MainPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main_page)

        val mainpageAdapter = MainpageAdapter()
        binding.recyclerViewGymInfoList.adapter = mainpageAdapter

        // GymInfoViewModel observe
    }



}