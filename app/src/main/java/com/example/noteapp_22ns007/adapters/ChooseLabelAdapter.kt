package com.example.noteapp_22ns007.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp_22ns007.databinding.ChooseLabelItemBinding
import com.example.noteapp_22ns007.model.database.entities.Label
import com.example.noteapp_22ns007.model.database.entities.NoteLabelCrossRef
import com.example.noteapp_22ns007.model.viewmodels.NoteViewModel

class ChooseLabelAdapter(
    private val noteId: Long,
    var labels: MutableList<Label>,
    var checkedLabels: MutableList<Label>,
    private val noteViewModel: NoteViewModel
): RecyclerView.Adapter<ChooseLabelAdapter.ChooseLabelViewHolder>() {
    private val viewHolderList = mutableListOf<ChooseLabelAdapter.ChooseLabelViewHolder>()

    inner class ChooseLabelViewHolder(val binding: ChooseLabelItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(label: Label) {
            binding.chooseLabelCheckBox.text = label.name
            binding.chooseLabelCheckBox.setOnCheckedChangeListener(null)
            binding.chooseLabelCheckBox.isChecked = checkedLabels.find { it.labelId == label.labelId } != null
            binding.chooseLabelCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked) {
                    noteViewModel.addLabelToNote(NoteLabelCrossRef(noteId, label.labelId!!))
                } else {
                    noteViewModel.removeLabelFromNote(noteId, label.labelId!!)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChooseLabelViewHolder {
        val binding = ChooseLabelItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = ChooseLabelViewHolder(binding)
        viewHolderList.add(viewHolder)
        return viewHolder
    }

    override fun getItemCount(): Int = labels.size

    override fun onBindViewHolder(holder: ChooseLabelViewHolder, position: Int) {
        holder.bind(labels[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateLabels(newLabels: List<Label>) {
        labels = newLabels.toMutableList()
        labels.sortedBy { checkedLabels.contains(it) }
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateCheckedLabels(newCheckedLabels: List<Label>) {
        checkedLabels = newCheckedLabels.toMutableList()
        notifyDataSetChanged()
    }
}