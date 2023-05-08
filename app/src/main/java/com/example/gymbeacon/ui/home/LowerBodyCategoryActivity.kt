package com.example.gymbeacon.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.gymbeacon.R
import com.example.gymbeacon.ViewModelFactory
import com.example.gymbeacon.databinding.ActivityLowerBodyCategoryBinding
import com.example.gymbeacon.ui.category.LowerBodyCategoryViewModel
import com.example.gymbeacon.ui.home.adapter.LowerBodyAdapter
import com.example.gymbeacon.ui.home.detail.DetailActivity
import com.example.gymbeacon.ui.home.detail.InfoDialogActivity


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

        lowerBodyAdapter.itemClick = object : LowerBodyAdapter.ItemClick{
            override fun onClick(view: View, position: Int) {
                when(position) {
                    0 -> goToDetailActivity("스쿼트")
                    1 -> goToDetailActivity("데드리프트")
                    2 -> goToDetailActivity("레그 익스텐션")
                }
            }

        }

        viewModel.items.observe(this) {
            Log.e("upper body page","items = $it")
            lowerBodyAdapter.submitList(it)
        }
    }

    fun goToDetailActivity(posValue: String) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("upper",posValue)
        startActivity(intent)
    }
}