package com.example.noteapp_22ns007.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp_22ns007.MainActivity
import com.example.noteapp_22ns007.Utils
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        mainActivity = activity as MainActivity
        addButton = binding.fabAdd
        noteRecyclerView = binding.noteRecyclerView
        noteViewModel = (activity as MainActivity).noteViewModel

        noteAdapter = NoteAdapter(emptyList(), mainActivity, this)
        binding.noteRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = noteAdapter
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val note = noteAdapter.notes[position].note

                mainActivity.noteViewModel.archive(note.noteId!!)
                Utils.notification(requireView(), "Đã lưu trữ ghi chú \"${note.title}\"", "Hoàn tác") {
                    mainActivity.noteViewModel.unArchive(note.noteId)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(noteRecyclerView)

        // Observe data from ViewModel and update Adapter
        noteViewModel.getNotesWithLabels().observe(viewLifecycleOwner) { notesWithLabels ->
//            Log.d("AllNote Debug: ", notesWithLabels.toString())
            allNotes = notesWithLabels
            allNotes.forEach {
                Log.d("QueryRs", it.note.debugStr())
            }

            if(allNotes.isEmpty()) {
                binding.noteRecyclerView.visibility = View.GONE
                binding.emptyNoteView.visibility = View.VISIBLE
            } else {
                binding.noteRecyclerView.visibility = View.VISIBLE
                binding.emptyNoteView.visibility = View.GONE
            }

            noteAdapter.updateNotes(allNotes)

            // Observe search value
            mainActivity.searchViewModel.searchQuery.removeObservers(viewLifecycleOwner)
            mainActivity.searchViewModel.searchQuery.observe(viewLifecycleOwner) {queryStr ->
                filterNotes(queryStr)
            }
        }

        addButton.setOnClickListener {
            mainActivity.noteViewModel.insert(Note(
                null, "", "", Date(), archived = false, pinned = false, deleted = false
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
                            insertedNote.note.noteId!!, "", "", insertedNote.note.dateCreated, insertedNote.note.pinned!!
                        )
                        mainActivity.displayEditNoteFragment(editNoteFragmentInstance)
                    }

                    mainActivity.searchViewModel.searchQuery.observe(viewLifecycleOwner) {queryStr ->
                        filterNotes(queryStr)
                        mainActivity.searchViewModel.searchQuery.removeObservers(viewLifecycleOwner)
                    }
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
            note.note.noteId!!, note.note.title, note.note.content, note.note.dateCreated, note.note.pinned!!
        )
        mainActivity.displayEditNoteFragment(editNoteFragmentInstance)
    }

    override fun onFirstItemSelected() {
        mainActivity.showOptions()
    }

    override fun onSelectionCleared() {
        mainActivity.hideOptions()
    }

    override fun onItemSelected() {
        mainActivity.checkPin()
    }

    override fun onItemUnselected() {
        mainActivity.checkPin()
    }

    fun clearSelections() = noteAdapter.clearSelections()

    fun getSelectedNotes() = noteAdapter.getSelectedNotes()

    private fun filterNotes(query: String) {
        val filteredNotes = getFilteredNotes(query)

        noteAdapter.updateNotes(filteredNotes)

        if(filteredNotes.isEmpty()) {
            binding.noteRecyclerView.visibility = View.GONE
            binding.emptyNoteView.visibility = View.VISIBLE
        } else {
            binding.noteRecyclerView.visibility = View.VISIBLE
            binding.emptyNoteView.visibility = View.GONE
        }
    }

    private fun getFilteredNotes(query: String): List<NoteWithLabels> {
        if (query.isBlank())
            return allNotes

        val parts = query.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val keywords = mutableListOf<String>()
        val labels = mutableListOf<String>()

        for (part in parts) {
            if (part.startsWith("$:")) {
                labels.add(part.substring(2).trim())
                continue
            }

            keywords.add(part)
        }

        return allNotes.filter { noteWithLabels ->
            val matchesKeywords = keywords.all { keyword ->
                noteWithLabels.note.title.contains(keyword, ignoreCase = true) ||
                        noteWithLabels.note.content.contains(keyword, ignoreCase = true) ||
                        noteWithLabels.labels.any { it.name.contains(keyword, ignoreCase = true) }
            }

            val matchesLabels = labels.all { label ->
                noteWithLabels.labels.any { it.name.equals(label, ignoreCase = false) }
            }

            matchesKeywords && matchesLabels
        }
    }
}