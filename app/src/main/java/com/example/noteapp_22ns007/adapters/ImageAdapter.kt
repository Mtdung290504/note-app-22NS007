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

class ImageAdapter(private var images: List<Image>) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = images[position]
        holder.bind(image.image)
    }

    override fun getItemCount(): Int = images.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateImage(newImages: List<Image>) {
        images = newImages
        notifyDataSetChanged()
    }

    class ImageViewHolder(private val binding: ImageItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imagePath: String) {
//            val imageFile = File(imagePath)
//            if (imageFile.exists()) {
//                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
//                binding.imageView.setImageBitmap(bitmap)
//            }
            @Suppress("DEPRECATION")
            Glide.with(itemView.context)
                .load(imagePath) // image.image là đường dẫn đến ảnh trong Image entity
                .thumbnail(0.3f)
                .into(binding.imageView)
        }
    }
}