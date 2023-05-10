package com.example.gymbeacon.ui.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.gymbeacon.R
import com.example.gymbeacon.ViewModelFactory
import com.example.gymbeacon.databinding.ActivityUpperBodyCategoryBinding
import com.example.gymbeacon.ui.category.CategoryViewModel
import com.example.gymbeacon.ui.home.adapter.UpperBodyAdapter
import com.example.gymbeacon.ui.home.detail.DetailActivity

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

        upperBodyAdapter.itemClick = object : UpperBodyAdapter.ItemClick{
            override fun onClick(view: View, position: Int) {
                Log.e("test", position.toString())
                when(position) {
                    0 -> goToDetailActivity("벤치프레스")
                    1 -> goToDetailActivity("랫 풀 다운")
                    2 -> goToDetailActivity("인클라인 벤치프레스")
                }
            }

        }

        viewModel.items.observe(this) {
            Log.e("upper body page","items = $it")
            upperBodyAdapter.submitList(it)
        }
    }

    fun goToDetailActivity(posValue: String) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("upper",posValue)
        startActivity(intent)
    }


}