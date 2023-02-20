package com.example.gymbeacon.ui.home

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.ActivityNewGymDetailBinding

class NewGymDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewGymDetailBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_new_gym_detail)
        binding.lifecycleOwner = this

        with(binding) {
            oneOne.setOnClickListener {
                oneOne.setBackgroundColor(Color.parseColor("#c4c4c4"))
            }
        }
    }
}