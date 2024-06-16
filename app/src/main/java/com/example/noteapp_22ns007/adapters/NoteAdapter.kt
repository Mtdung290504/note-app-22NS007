package com.example.noteapp_22ns007.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.trimmedLength
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp_22ns007.databinding.NoteItemBinding
import com.example.noteapp_22ns007.model.database.entities.relationshipClasses.NoteWithLabels

class NoteAdapter(private var notes: List<NoteWithLabels>, private val clickListener: NoteClickListener) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = NoteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding, clickListener)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.bind(note)
    }

    override fun getItemCount(): Int = notes.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateNotes(newNotes: List<NoteWithLabels>) {
        this.notes = newNotes
        notifyDataSetChanged()
    }

    class NoteViewHolder(private val binding: NoteItemBinding, private val clickListener: NoteClickListener) : RecyclerView.ViewHolder(binding.root) {
        fun bind(note: NoteWithLabels) {
            binding.noteTitle.text = note.note.title
            binding.noteContent.text = note.note.content

            if(binding.noteTitle.text.isNullOrBlank())
                binding.noteTitle.visibility = View.GONE
            else
                binding.noteTitle.visibility = View.VISIBLE

            if(binding.noteContent.text.isNullOrBlank())
                binding.noteContent.visibility = View.GONE
            else
                binding.noteContent.visibility = View.VISIBLE

            if(note.labels.isNotEmpty()) {
                val labelAdapter = LabelAdapter(note.labels)
                binding.labelRecyclerView.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
                binding.labelRecyclerView.adapter = labelAdapter
            }

            binding.root.setOnClickListener {
                clickListener.onNoteClick(note)
            }
        }
    }

    interface NoteClickListener {
        fun onNoteClick(note: NoteWithLabels)
    }
}