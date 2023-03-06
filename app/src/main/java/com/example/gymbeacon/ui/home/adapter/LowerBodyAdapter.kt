package com.example.gymbeacon.ui.home.adapter

import com.example.gymbeacon.databinding.ItemLowerBodyCategoryBinding
import com.example.gymbeacon.model.LowerBodyCategory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymbeacon.GlideApp

class LowerBodyAdapter: ListAdapter<LowerBodyCategory,LowerBodyAdapter.LowerBodyViewHolder>(LowerBodyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LowerBodyViewHolder {
        val binding = ItemLowerBodyCategoryBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return LowerBodyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LowerBodyViewHolder, position: Int) {

        GlideApp.with(holder.shapeableImageView)
            .load(getItem(position).thumbnailImageUrl)
            .into(holder.shapeableImageView)

        holder.bind(getItem(position))
    }

    class LowerBodyViewHolder(private val binding: ItemLowerBodyCategoryBinding) : RecyclerView.ViewHolder(binding.root){
        val shapeableImageView = binding.itemLowerBodyImage

        fun bind(category: LowerBodyCategory) {
            binding.category = category
            binding.executePendingBindings()
        }
    }

}

class LowerBodyDiffCallback : DiffUtil.ItemCallback<LowerBodyCategory>(){
    override fun areItemsTheSame(oldItem: LowerBodyCategory, newItem: LowerBodyCategory): Boolean {
        return oldItem.categoryId == newItem.categoryId
    }

    override fun areContentsTheSame(oldItem: LowerBodyCategory, newItem: LowerBodyCategory): Boolean {
        return oldItem == newItem
    }

}