package com.example.gymbeacon.ui.home.detail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_detail)
        binding.lifecycleOwner = this

        val detailIntent = intent
        val selectedExerciseName = detailIntent.getStringExtra("upper")

        with(binding) {
            textViewExerciseName.text = selectedExerciseName
        }
        initEvent()
        Log.e("detail","$selectedExerciseName")
    }

    private fun initEvent() {
        with (binding) {
            minusButton.setOnClickListener{
                val currentCount = textViewDetailPageCount.text.toString()
                val nowCount = currentCount.toInt() - 1
                textViewDetailPageCount.text = nowCount.toString()
            }

            plusButton.setOnClickListener {
                val currentCount = textViewDetailPageCount.text.toString()
                val nowCount = currentCount.toInt() + 1
                textViewDetailPageCount.text = nowCount.toString()
            }
        }
    }
}