package com.example.gymbeacon.ui.mainpage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymbeacon.databinding.MainpageItemLayoutBinding
import com.example.gymbeacon.model.GymInfo

class MainpageAdapter: ListAdapter<GymInfo,MainpageAdapter.MainpageViewHolder>(MainpageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainpageViewHolder {
        val binding = MainpageItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MainpageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainpageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MainpageViewHolder(private val binding: MainpageItemLayoutBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(gymInfo: GymInfo) {
            binding.gyminfo = gymInfo
            binding.executePendingBindings()
        }
    }

}

class MainpageDiffCallback : DiffUtil.ItemCallback<GymInfo>(){
    override fun areItemsTheSame(oldItem: GymInfo, newItem: GymInfo): Boolean {
        return oldItem.gymName == newItem.gymName
    }

    override fun areContentsTheSame(oldItem: GymInfo, newItem: GymInfo): Boolean {
        return oldItem == newItem
    }

}