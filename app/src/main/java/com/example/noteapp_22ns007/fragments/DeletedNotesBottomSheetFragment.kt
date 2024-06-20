package com.example.noteapp_22ns007.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp_22ns007.MainActivity
import com.example.noteapp_22ns007.Utils
import com.example.noteapp_22ns007.adapters.NoteAdapter
import com.example.noteapp_22ns007.databinding.FragmentDeleteNotesBottomSheetBinding
import com.example.noteapp_22ns007.model.database.entities.Image
import com.example.noteapp_22ns007.model.database.entities.relationshipClasses.NoteWithLabels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File

class DeletedNotesBottomSheetFragment : BottomSheetDialogFragment(), NoteAdapter.NoteClickListener {
    private var _binding: FragmentDeleteNotesBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainActivity: MainActivity

    private lateinit var noteAdapter: NoteAdapter
    private lateinit var deletedNoteLiveData: LiveData<List<NoteWithLabels>>
    private var allNotes: List<NoteWithLabels> = emptyList()

    private var imageLiveData: LiveData<List<Image>>? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeleteNotesBottomSheetBinding.inflate(inflater, container, false)

        mainActivity = activity as MainActivity

        noteAdapter = NoteAdapter(allNotes, mainActivity, this)
        binding.trashRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = noteAdapter
        }

        deletedNoteLiveData = mainActivity.noteViewModel.getDeletedNoteWithLabels()
        deletedNoteLiveData.observe(viewLifecycleOwner) {noteAndLabels ->
            allNotes = noteAndLabels
            allNotes.forEach {
                Log.d("DeleteQueryRs", it.note.debugStr())
            }

            if(allNotes.isEmpty()) {
                binding.trashRecyclerView.visibility = View.GONE
                binding.emptyTrashView.visibility = View.VISIBLE
            } else {
                binding.trashRecyclerView.visibility = View.VISIBLE
                binding.emptyTrashView.visibility = View.GONE
            }

            noteAdapter.updateNotes(allNotes)
        }

        binding.cancelBtn.setOnClickListener {
            clearSelections()
            hideOptions()
        }

        binding.recoverBtn.setOnClickListener {
            val selectedNotes = getSelectedNotes()

            mainActivity.noteViewModel.removeAllFromTrash(selectedNotes.map { it.note.noteId!! })
            clearSelections()
            hideOptions()
        }

        binding.deleteBtn.setOnClickListener {
            val selectedNotes = getSelectedNotes()
            val selectedNoteIds = selectedNotes.map { it.note.noteId!! }

            AlertDialog.Builder(context).apply {
                setTitle("Xóa vĩnh viễn ghi chú")
                setMessage("\nBạn có chắc chắn muốn xóa vĩnh viễn các ghi chú này?")
                setPositiveButton("Xóa vĩnh viễn") { _, _ ->
                    imageLiveData = mainActivity.imageViewModel.getImageOfAllNotes(selectedNoteIds)
                    imageLiveData!!.observe(viewLifecycleOwner) { images ->
                        images.forEach{
                            val path = it.image
                            val file = File(path)
                            if(file.exists()) {
                                Log.d("DELETE IMG", "Deleted file: $path")
                                file.delete()
                            } else {
                                Log.d("DELETE IMG", "File not found!: $path")
                            }
                        }
                        mainActivity.noteViewModel.deleteAllNoteByIds(selectedNoteIds)
                        mainActivity.imageViewModel.deleteImageOfAllNote(selectedNoteIds)
                        imageLiveData?.removeObservers(viewLifecycleOwner)
                    }
                    clearSelections()
                    hideOptions()
                }
                setNegativeButton("Hủy") { dialog, _ ->
                    dialog.dismiss()
                }
                create()
                show()
            }
        }

        setupSwipeHandler()

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun setupSwipeHandler() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val note = noteAdapter.notes[position].note

                AlertDialog.Builder(context).apply {
                    setTitle("Xóa vĩnh viễn ghi chú")
                    setMessage("\nBạn có chắc chắn muốn xóa vĩnh viễn ghi chú này?")
                    setPositiveButton("Xóa vĩnh viễn") { _, _ ->
                        val noteTitle = note.title
                        imageLiveData = mainActivity.imageViewModel.getImageOfNote(note.noteId!!)
                        imageLiveData!!.observe(viewLifecycleOwner) { images ->
                            images.forEach{
                                val path = it.image
                                val file = File(path)
                                if(file.exists()) {
                                    Log.d("DELETE IMG", "Deleted file: $path")
                                    file.delete()
                                } else {
                                    Log.d("DELETE IMG", "File not found!: $path")
                                }
                            }
                            mainActivity.noteViewModel.deleteNoteById(note.noteId)
                            mainActivity.imageViewModel.deleteImageOfNote(note.noteId)
                            imageLiveData?.removeObservers(viewLifecycleOwner)
                        }
                        Utils.notification(view, "Đã xóa note \"$noteTitle\"", "") {}
                    }
                    setNegativeButton("Hủy") { dialog, _ ->
                        noteAdapter.notifyDataSetChanged()
                        dialog.dismiss()
                    }
                    create()
                    show()
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.trashRecyclerView)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onNoteClick(note: NoteWithLabels) {
        AlertDialog.Builder(context).apply {
            setTitle("Khôi phục ghi chú")
            setMessage("\nBạn có chắc chắn muốn khôi phục ghi chú này?")
            setPositiveButton("Khôi phục") { _, _ ->
                mainActivity.noteViewModel.removeFromTrash(note.note.noteId!!)
            }
            setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    private fun View.fadeIn(duration: Long = 300) {
        this.visibility = View.VISIBLE
        val fadeIn = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f)
        fadeIn.duration = duration
        fadeIn.start()
    }

    private fun View.fadeOut(duration: Long = 300, onEnd: () -> Unit = {}) {
        val fadeOut = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f)
        fadeOut.duration = duration
        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                this@fadeOut.visibility = View.GONE
                onEnd()
            }
        })
        fadeOut.start()
    }

    private fun displayOptions() {
        binding.multipleChoiceCtn.fadeIn()
    }

    private fun hideOptions() {
        binding.multipleChoiceCtn.fadeOut()
    }

    private fun clearSelections() = noteAdapter.clearSelections()

    private fun getSelectedNotes() = noteAdapter.getSelectedNotes()

    override fun onFirstItemSelected() {
        displayOptions()
    }

    override fun onSelectionCleared() {
        hideOptions()
    }

    override fun onItemSelected() {
        return
    }

    override fun onItemUnselected() {
        return
    }
}