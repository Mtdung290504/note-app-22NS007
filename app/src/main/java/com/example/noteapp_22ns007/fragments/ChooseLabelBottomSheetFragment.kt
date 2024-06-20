package com.example.noteapp_22ns007.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp_22ns007.MainActivity
import com.example.noteapp_22ns007.Utils
import com.example.noteapp_22ns007.adapters.ChooseLabelAdapter
import com.example.noteapp_22ns007.adapters.LabelAdapter
import com.example.noteapp_22ns007.databinding.FragmentChooseLabelBottomSheetBinding
import com.example.noteapp_22ns007.model.database.entities.Label
import com.example.noteapp_22ns007.model.database.entities.NoteLabelCrossRef
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Date

class ChooseLabelBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentChooseLabelBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var noteId: Long = 0
    private lateinit var labelLiveData: LiveData<List<Label>>
    private var labels: List<Label> = listOf()

    // Choose label adapter
    private lateinit var chooseLabelAdapter: ChooseLabelAdapter
    private lateinit var checkedLabelLiveData: LiveData<List<Label>>
    private var checkedLabels: List<Label> = listOf()

    private var addLabelSuccessLiveData: LiveData<Boolean>? = null

    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChooseLabelBottomSheetBinding.inflate(inflater, container, false)

        initializeViewElements()

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            noteId = it.getLong(NOTE_ID)
        }
    }

    private fun initializeViewElements() {
        mainActivity = activity as MainActivity

        // Observe labels
        labelLiveData = mainActivity.labelViewModel.getAllLabels()
        labelLiveData.observe(viewLifecycleOwner) {
            labels = it
            chooseLabelAdapter.updateLabels(labels)
        }

        // Observe checked labels
        checkedLabelLiveData = mainActivity.labelViewModel.getLabelsByNoteId(noteId)
        checkedLabelLiveData.observe(viewLifecycleOwner) { labelOfNote ->
            checkedLabels = labelOfNote
            chooseLabelAdapter.updateCheckedLabels(checkedLabels) // Update checked labels
        }

        binding.addLabelBtn.setOnClickListener {
            val newLabelName = binding.addLabelEdt.text.toString()
            if(newLabelName.isBlank()) {
                Utils.notification(view, "Nhãn không được trống", "") {}
                return@setOnClickListener
            }

            mainActivity.labelViewModel.insert(Label(null, newLabelName))

            addLabelSuccessLiveData = mainActivity.labelViewModel.insertResult
            addLabelSuccessLiveData?.observe(viewLifecycleOwner) { success ->
                if (!success) {
                    Utils.notification(view, "Nhãn \"$newLabelName\" đã tồn tại", "") {}
                } else {
                    Utils.notification(view, "Đã thêm nhãn \"$newLabelName\"", "") {}
                    binding.addLabelEdt.text = null
                    mainActivity.noteViewModel.addLabelToNote(
                        NoteLabelCrossRef(noteId, mainActivity.labelViewModel.insertedLabelId)
                    )
                    binding.addLabelEdt.clearFocus()
                    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.addLabelEdt.windowToken, 0)
                }
                addLabelSuccessLiveData!!.removeObservers(viewLifecycleOwner)
            }
        }

        // Initialize adapter for choose label fragment
        chooseLabelAdapter = ChooseLabelAdapter(
            noteId, labels.toMutableList(),
            checkedLabels.toMutableList(),
            mainActivity.noteViewModel
        )
        binding.chooseLabelRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chooseLabelAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private const val NOTE_ID = "noteId"

        @JvmStatic
        fun newInstance(noteId: Long): ChooseLabelBottomSheetFragment {
            return ChooseLabelBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putLong(NOTE_ID, noteId)
                }
            }
        }
    }
}