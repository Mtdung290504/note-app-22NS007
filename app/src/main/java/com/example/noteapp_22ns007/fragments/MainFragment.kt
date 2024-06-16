package com.example.noteapp_22ns007.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp_22ns007.MainActivity
import com.example.noteapp_22ns007.adapters.NoteAdapter
import com.example.noteapp_22ns007.databinding.FragmentMainBinding
import com.example.noteapp_22ns007.model.database.entities.Note
import com.example.noteapp_22ns007.model.database.entities.relationshipClasses.NoteWithLabels
import com.example.noteapp_22ns007.model.viewmodels.NoteViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Date

class MainFragment : Fragment(), NoteAdapter.NoteClickListener {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var addButton: FloatingActionButton
    private lateinit var noteRecyclerView: RecyclerView
    private lateinit var mainActivity: MainActivity
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var noteAdapter: NoteAdapter

    private var currentNoteLiveData: LiveData<NoteWithLabels>? = null
    private var allNotes: List<NoteWithLabels> = emptyList()
    private var keywordToFilter: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        mainActivity = activity as MainActivity
        addButton = binding.fabAdd
        noteRecyclerView = binding.noteRecyclerView
        noteViewModel = (activity as MainActivity).noteViewModel

        noteAdapter = NoteAdapter(emptyList(), this)
        binding.noteRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = noteAdapter
        }

        // Observe data from ViewModel and update Adapter
        noteViewModel.getNotesWithLabels().observe(viewLifecycleOwner) { notesWithLabels ->
            allNotes = notesWithLabels
            if(allNotes.isEmpty()) {
                binding.noteRecyclerView.visibility = View.GONE
                binding.emptyNoteView.visibility = View.VISIBLE
            } else {
                binding.noteRecyclerView.visibility = View.VISIBLE
                binding.emptyNoteView.visibility = View.GONE
            }
            noteAdapter.updateNotes(allNotes)
        }

        addButton.setOnClickListener {
            mainActivity.noteViewModel.insert(Note(
                null, "", "", Date()
            ))

            // Add a new observer for SingleLiveEvent
            mainActivity.noteViewModel.insertedNoteId.observe(viewLifecycleOwner) { insertedNoteId ->
                // Remove the old observer if it exists
                currentNoteLiveData?.removeObservers(viewLifecycleOwner)

                // Get the new LiveData and observe it
                currentNoteLiveData = mainActivity.noteViewModel.getNoteById(insertedNoteId)
                currentNoteLiveData?.observe(viewLifecycleOwner) { insertedNote ->
                    if (insertedNoteId != -1L) {
                        // Check if the change note is not due to update or deletion
                        Log.d("MainFragment", insertedNote.toString())
                        val editNoteFragmentInstance = EditNoteFragment.newInstance(
                            insertedNote.note.noteId!!, "", "", insertedNote.note.dateCreated, insertedNote.labels
                        )
                        mainActivity.displayEditNoteFragment(editNoteFragmentInstance)
                    }
                    filterNotes(keywordToFilter)
                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onNoteClick(note: NoteWithLabels) {
        val editNoteFragmentInstance = EditNoteFragment.newInstance(
            note.note.noteId!!, note.note.title, note.note.content, note.note.dateCreated, note.labels
        )
        mainActivity.displayEditNoteFragment(editNoteFragmentInstance)
    }

    fun filterNotes(query: String) {
        if(query.isBlank()) {
            noteAdapter.updateNotes(allNotes)
            keywordToFilter = ""
            return
        }

        val filteredNotes = allNotes.filter {
            it.note.title.contains(query, ignoreCase = true)
                    || it.note.content.contains(query, ignoreCase = true)
                    || it.labels.map {
                        label -> label.name
                    }.toString().contains(query, ignoreCase = true)
        }

        noteAdapter.updateNotes(filteredNotes)
        keywordToFilter = query

        if(filteredNotes.isEmpty()) {
            binding.noteRecyclerView.visibility = View.GONE
            binding.emptyNoteView.visibility = View.VISIBLE
        } else {
            binding.noteRecyclerView.visibility = View.VISIBLE
            binding.emptyNoteView.visibility = View.GONE
        }
    }
}