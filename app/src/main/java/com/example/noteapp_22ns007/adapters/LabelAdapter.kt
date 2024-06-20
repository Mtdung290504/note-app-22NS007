package com.example.noteapp_22ns007.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp_22ns007.MainActivity
import com.example.noteapp_22ns007.databinding.LabelItemBinding
import com.example.noteapp_22ns007.model.database.entities.Label

class LabelAdapter(
    private var labels: List<Label>,
    private val mainActivity: MainActivity,
    private var margin: Boolean?
):
    RecyclerView.Adapter<LabelAdapter.LabelViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelViewHolder {
        val binding = LabelItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LabelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LabelViewHolder, position: Int) {
        holder.bind(labels[position])
    }

    override fun getItemCount(): Int = labels.size

    inner class LabelViewHolder(private val binding: LabelItemBinding): RecyclerView.ViewHolder(binding.root)  {
        fun bind(label: Label) {
            binding.labelName.text = label.name

            if(margin == true) {
                val layoutParams = binding.root.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.bottomMargin = 30
            }

            binding.root.setOnClickListener {
                mainActivity.searchLabel(label.name)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateLabel(labels: List<Label>) {
        this.labels = labels
        notifyDataSetChanged()
    }
}