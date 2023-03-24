package com.example.gymbeacon.ui.home.detail

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.ActivityDetailBinding
import com.example.gymbeacon.ui.home.camera.CameraActivity

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var selectedExerciseName : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_detail)
        binding.lifecycleOwner = this

        val detailIntent = intent
        selectedExerciseName = detailIntent.getStringExtra("upper")!!

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

            buttonDetailStart.setOnClickListener {
                goToCameraActivity()
            }
        }
    }

    fun goToCameraActivity() {
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra("maxnum",binding.textViewDetailPageCount.text)
        intent.putExtra("selectedExerciseName",selectedExerciseName)
        startActivity(intent)
    }
}