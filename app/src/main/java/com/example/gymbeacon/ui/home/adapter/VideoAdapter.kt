package com.example.gymbeacon.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gymbeacon.databinding.ItemVideoBinding
import com.example.gymbeacon.model.Video

class VideoAdapter(private val videos: List<Video>, val onClick: (Int) -> Unit) :
    RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root) {
        val titleTextView = binding.textViewFileNameInput
        val playButton = binding.imageViewVideoPlayButton
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val video = videos[position]
        holder.titleTextView.text = video.title
        holder.playButton.setOnClickListener {
            onClick(position)
        }
    }

    override fun getItemCount(): Int {
        return videos.size
    }
}
