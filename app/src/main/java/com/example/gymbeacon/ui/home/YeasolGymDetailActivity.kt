package com.example.gymbeacon.ui.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.ActivityYeasolGymDetailBinding

class YeasolGymDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityYeasolGymDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_yeasol_gym_detail)
        binding.lifecycleOwner = this
    }
}