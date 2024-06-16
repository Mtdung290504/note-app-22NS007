package com.example.noteapp_22ns007.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp_22ns007.R
import com.example.noteapp_22ns007.databinding.ManageLabelItemBinding
import com.example.noteapp_22ns007.model.database.entities.Label

class ManageLabelAdapter(private var labels: MutableList<Label>) : RecyclerView.Adapter<ManageLabelAdapter.ManageLabelViewHolder>() {

    inner class ManageLabelViewHolder(private val binding: ManageLabelItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(label: Label) {
            binding.editTextManageLabel.setText(label.name)

            binding.editTextManageLabel.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.iconManageLabel.setImageResource(R.drawable.z_ic_delete_note)
                    binding.iconEditManageLabel.setImageResource(R.drawable.z_ic_check)
                } else {
                    binding.iconManageLabel.setImageResource(R.drawable.z_ic_label)
                    binding.iconEditManageLabel.setImageResource(R.drawable.z_ic_edit)
                }
            }

            binding.iconEditManageLabel.setOnClickListener {
                if (binding.editTextManageLabel.isFocused) {
                    val newLabel = binding.editTextManageLabel.text.toString()

                    labels[adapterPosition] = Label(label.labelId, newLabel)
                    notifyItemChanged(adapterPosition)
                } else {
                    binding.editTextManageLabel.requestFocus()
                }
            }

            binding.iconManageLabel.setOnClickListener {
                if (binding.editTextManageLabel.isFocused) {
                    labels.removeAt(adapterPosition)
                    notifyItemRemoved(adapterPosition)
                    notifyItemRangeChanged(adapterPosition, labels.size)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageLabelViewHolder {
        val binding = ManageLabelItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ManageLabelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ManageLabelViewHolder, position: Int) {
        holder.bind(labels[position])
    }

    override fun getItemCount(): Int = labels.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateLabels(newLabels: List<Label>) {
        labels = newLabels.toMutableList()
        notifyDataSetChanged()
    }
}