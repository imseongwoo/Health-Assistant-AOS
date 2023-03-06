package com.example.gymbeacon.ui.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.gymbeacon.R
import com.example.gymbeacon.ViewModelFactory
import com.example.gymbeacon.databinding.ActivityLowerBodyCategoryBinding
import com.example.gymbeacon.ui.category.CategoryViewModel
import com.example.gymbeacon.ui.category.LowerBodyCategoryViewModel
import com.example.gymbeacon.ui.home.adapter.LowerBodyAdapter
import com.example.gymbeacon.ui.mainpage.UpperBodyAdapter

class LowerBodyCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLowerBodyCategoryBinding
    // viewmodel 변경 필요
    private val viewModel: LowerBodyCategoryViewModel by viewModels { ViewModelFactory() }

    val lowerBodyAdapter = LowerBodyAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_lower_body_category)
        binding.lifecycleOwner = this

        with(binding) {
            recyclerViewLowerBodyCategoryList.adapter = lowerBodyAdapter
        }

        viewModel.items.observe(this) {
            Log.e("upper body page","items = $it")
            lowerBodyAdapter.submitList(it)
        }
    }
}