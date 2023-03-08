package com.example.gymbeacon.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymbeacon.GlideApp
import com.example.gymbeacon.databinding.ItemUpperBodyCategoryBinding
import com.example.gymbeacon.model.Category

class UpperBodyAdapter: ListAdapter<Category, UpperBodyAdapter.UpperBodyViewHolder>(
    UpperBodyDiffCallback()) {

    interface ItemClick{
        fun onClick(view: View, position: Int)
    }

    var itemClick: ItemClick? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpperBodyViewHolder {
        val binding = ItemUpperBodyCategoryBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return UpperBodyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UpperBodyViewHolder, position: Int) {

        GlideApp.with(holder.shapeableImageView)
            .load(getItem(position).thumbnailImageUrl)
            .into(holder.shapeableImageView)

        holder.bind(getItem(position))

        if (itemClick != null){
            holder.binding2.constraintLayoutItemUpperBodyCategory.setOnClickListener(View.OnClickListener {
                itemClick?.onClick(it, position)
            })
        }
    }

    class UpperBodyViewHolder(private val binding: ItemUpperBodyCategoryBinding) : RecyclerView.ViewHolder(binding.root){
        val shapeableImageView = binding.itemUpperBodyImage
        val binding2 = binding
        fun bind(category: Category) {
            binding.category = category
            binding.executePendingBindings()
        }
    }

}

class UpperBodyDiffCallback : DiffUtil.ItemCallback<Category>(){
    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem.categoryId == newItem.categoryId
    }

    override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem == newItem
    }

}