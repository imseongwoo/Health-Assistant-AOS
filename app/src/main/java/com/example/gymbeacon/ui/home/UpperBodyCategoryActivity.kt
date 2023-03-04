package com.example.gymbeacon.ui.home

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.gymbeacon.R
import com.example.gymbeacon.databinding.ActivityUpperBodyCategoryBinding

class UpperBodyCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpperBodyCategoryBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_upper_body_category)
        binding.lifecycleOwner = this

        with(binding) {
            oneOne.setOnClickListener {
                oneOne.setBackgroundColor(Color.parseColor("#c4c4c4"))
            }
        }
    }
}