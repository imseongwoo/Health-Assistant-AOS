package com.example.gymbeacon.ui.mainpage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.gymbeacon.R
import com.example.gymbeacon.ViewModelFactory
import com.example.gymbeacon.databinding.ActivityMainPageBinding
import com.example.gymbeacon.ui.gyminfo.GymInfoViewModel

class MainPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainPageBinding
    private val viewModel: GymInfoViewModel by viewModels {ViewModelFactory()}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main_page)

        val mainpageAdapter = MainpageAdapter()
        binding.recyclerViewGymInfoList.adapter = mainpageAdapter

        // GymInfoViewModel observe
        viewModel.items.observe(this@MainPageActivity) {
            Log.e("mainpage","items = $it")
            mainpageAdapter.submitList(it)
        }

    }



}