package com.example.gymbeacon.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gymbeacon.databinding.ItemMyPageBinding

class MyPageViewPagerAdapter(private val exerciseCountMap: MutableMap<String, Pair<Int, Int>>) :
    RecyclerView.Adapter<MyPageViewPagerAdapter.PagerViewHolder>() {

    inner class PagerViewHolder(private val binding: ItemMyPageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val textView = binding.textViewItem
        val sectextview = binding.textViewSecondItem
        val thirdTextView = binding.textViewThirdItem
        val fouthTextView = binding.textViewFourthItem
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        val binding = ItemMyPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PagerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        holder.textView.text = exerciseCountMap.keys.toList()[position]
        holder.sectextview.text = (exerciseCountMap[holder.textView.text]?.first ?: 0).toString()
        holder.thirdTextView.text = (exerciseCountMap[holder.textView.text]?.second ?: 0).toString()
        holder.fouthTextView.text = getAverage(holder.textView.text as String).toString()
    }

    override fun getItemCount(): Int {
        return exerciseCountMap.size
    }

    private fun getAverage(exer: String): Int? {
        val sum = exerciseCountMap[exer]?.first?.toInt()
        val num = exerciseCountMap[exer]?.second?.toInt()
        return if (num!! > 0) sum?.div(num) else 0
    }
}