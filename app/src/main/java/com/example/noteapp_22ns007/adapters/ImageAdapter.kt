package com.example.noteapp_22ns007.adapters

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp_22ns007.databinding.ImageItemBinding

class ImageAdapter(private var images: MutableList<Bitmap>) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = images[position]
        holder.bind(image)
    }

    override fun getItemCount(): Int = images.size

    fun addImage(imgBitmap: Bitmap) {
        this.images.add(imgBitmap)
        notifyItemInserted(images.size - 1)
    }

    class ImageViewHolder(private val binding: ImageItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(image: Bitmap) {
            binding.imageView.setImageBitmap(image)
        }
    }
}