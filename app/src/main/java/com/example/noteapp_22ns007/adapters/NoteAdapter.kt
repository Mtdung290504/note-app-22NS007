package com.example.noteapp_22ns007.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp_22ns007.MainActivity
import com.example.noteapp_22ns007.R
import com.example.noteapp_22ns007.databinding.NoteItemBinding
import com.example.noteapp_22ns007.model.database.entities.relationshipClasses.NoteWithLabels

class NoteAdapter(
    var notes: List<NoteWithLabels>,
    private val mainActivity: MainActivity,
    private val clickListener: NoteClickListener
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    private val selectedItems = mutableSetOf<Long>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = NoteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding, clickListener)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.bind(note, selectedItems.contains(note.note.noteId))
    }

    override fun getItemCount(): Int = notes.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateNotes(newNotes: List<NoteWithLabels>) {
        this.notes = newNotes
        notifyDataSetChanged()
    }

    inner class NoteViewHolder(private val binding: NoteItemBinding, private val clickListener: NoteClickListener) : RecyclerView.ViewHolder(binding.root) {
        fun bind(note: NoteWithLabels, isSelected: Boolean) {
            val drawableStart = ContextCompat.getDrawable(mainActivity, R.drawable.z_ic_pin)

            binding.noteTitle.text = note.note.title
            binding.noteContent.text = note.note.content

            binding.noteTitle.visibility = if (binding.noteTitle.text.isNullOrBlank()) View.GONE else View.VISIBLE
            binding.noteContent.visibility = if (binding.noteContent.text.isNullOrBlank()) View.GONE else View.VISIBLE
            if(note.note.pinned == true)
                binding.noteTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableStart, null, null, null)
            else
                binding.noteTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
            if(note.note.pinned == true) Log.d("DebugPin", note.note.title)

            if (note.labels.isNotEmpty()) {
                binding.labelRecyclerView.visibility = View.VISIBLE

                val labelAdapter = LabelAdapter(note.labels, mainActivity, false)
                binding.labelRecyclerView.apply {
                    layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = labelAdapter
                }

                Log.d("Debug note", "Id: ${note.note.noteId}, Title: ${note.note.title}, Labels: ${note.labels}")
            } else {
                binding.labelRecyclerView.visibility = View.GONE
            }

            // Thay đổi giao diện khi item được chọn
            if (isSelected) {
                binding.root.setBackgroundResource(R.drawable.z_note_item_selected_background)
                clickListener.onItemSelected()
            } else {
                binding.root.setBackgroundResource(R.drawable.z_note_item_background)
                clickListener.onItemUnselected()
            }

            binding.root.setOnClickListener {
                if (selectedItems.contains(note.note.noteId)) {
                    selectedItems.remove(note.note.noteId)
                    notifyItemChanged(adapterPosition)

                    // Kiểm tra nếu item cuối cùng bị hủy chọn
                    if (selectedItems.isEmpty()) {
                        clickListener.onSelectionCleared()
                    }
                    return@setOnClickListener
                }

                if(selectedItems.isNotEmpty()) {
                    if (!selectedItems.contains(note.note.noteId)) {
                        selectedItems.add(note.note.noteId!!)
                    }
                    notifyItemChanged(adapterPosition)
                    return@setOnClickListener
                }

                clickListener.onNoteClick(note)
            }

            binding.root.setOnLongClickListener {
                if (!selectedItems.contains(note.note.noteId)) {
                    selectedItems.add(note.note.noteId!!)

                    // Kiểm tra item đầu tiên được chọn
                    if (selectedItems.size == 1) {
                        clickListener.onFirstItemSelected()
                    }
                }
                notifyItemChanged(adapterPosition)
                true
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearSelections() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun getSelectedNotes(): List<NoteWithLabels> {
        return notes.filter { selectedItems.contains(it.note.noteId) }
    }

    interface NoteClickListener {
        fun onNoteClick(note: NoteWithLabels)
        fun onFirstItemSelected()
        fun onSelectionCleared()
        fun onItemSelected()
        fun onItemUnselected()
    }
}