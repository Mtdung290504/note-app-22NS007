package com.example.noteapp_22ns007.adapters

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.noteapp_22ns007.databinding.ImageItemBinding
import com.example.noteapp_22ns007.model.database.entities.Image
import java.io.File

class ImageAdapter(var images: List<Image>, private var longClickListener: OnImageLongClickListener) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = images[position]
        holder.bind(image)
    }

    override fun getItemCount(): Int = images.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateImage(newImages: List<Image>) {
        images = newImages
        notifyDataSetChanged()
    }

    inner class ImageViewHolder(private val binding: ImageItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(image: Image) {
            @Suppress("DEPRECATION")
            Glide.with(itemView.context)
                .load(File(image.image)) // Sử dụng đường dẫn của ảnh từ Image entity
                .thumbnail(0.3f)
                .into(binding.imageView)

            binding.root.setOnLongClickListener {
                longClickListener.onImageLongClick(image)
                true
            }
        }
    }

    fun setOnImageLongClickListener(listener: OnImageLongClickListener) {
        this.longClickListener = listener
    }

    interface OnImageLongClickListener {
        fun onImageLongClick(image: Image)
    }
}