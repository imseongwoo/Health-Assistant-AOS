package com.example.gymbeacon.ui.home

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.gymbeacon.R
import com.example.gymbeacon.ViewModelFactory
import com.example.gymbeacon.databinding.ActivityUpperBodyCategoryBinding
import com.example.gymbeacon.ui.category.CategoryViewModel
import com.example.gymbeacon.ui.mainpage.UpperBodyAdapter

class UpperBodyCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpperBodyCategoryBinding
    private val viewModel: CategoryViewModel by viewModels { ViewModelFactory() }
    val upperBodyAdapter = UpperBodyAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_upper_body_category)
        binding.lifecycleOwner = this

        with(binding) {
            recyclerViewUpperBodyCategoryList.adapter = upperBodyAdapter
        }

        viewModel.items.observe(this) {
            Log.e("upper body page","items = $it")
            upperBodyAdapter.submitList(it)
        }
    }


}