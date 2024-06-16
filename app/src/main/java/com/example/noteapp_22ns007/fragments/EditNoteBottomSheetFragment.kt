package com.example.noteapp_22ns007.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.noteapp_22ns007.databinding.EditNoteBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EditNoteBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: EditNoteBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var takePictureClickHandler: () -> Unit
    private lateinit var getPictureClickHandler: () -> Unit
    private lateinit var deleteClickHandler: () -> Unit
    private lateinit var labelClickHandler: () -> Unit
    private lateinit var pinClickHandler: () -> Unit
    private lateinit var archiveClickHandler: () -> Unit

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = EditNoteBottomSheetBinding.inflate(inflater, container, false)

        clearListeners()
        initializeViewElements()

        return binding.root
    }

    private fun initializeViewElements() {
        binding.apply {
            takePicture.setOnClickListener {
                takePictureClickHandler()
            }
            getPicture.setOnClickListener {
                getPictureClickHandler()
            }
            delete.setOnClickListener {
                deleteClickHandler()
            }
            label.setOnClickListener {
                labelClickHandler()
            }
            pin.setOnClickListener {
                pinClickHandler()
            }
            archive.setOnClickListener {
                archiveClickHandler()
            }
        }
    }

    private fun clearListeners() {
        binding.takePicture.setOnClickListener(null)
        binding.getPicture.setOnClickListener(null)
        binding.delete.setOnClickListener(null)
        binding.label.setOnClickListener(null)
        binding.pin.setOnClickListener(null)
        binding.archive.setOnClickListener(null)
    }

    fun setTakePictureListener(takePictureClickHandler: () -> Unit): EditNoteBottomSheetFragment {
        this.takePictureClickHandler = takePictureClickHandler
        return this
    }

    fun setGetPictureListener(getPictureClickHandler: () -> Unit): EditNoteBottomSheetFragment {
        this.getPictureClickHandler = getPictureClickHandler
        return this
    }

    fun setDeleteListener(deleteClickHandler: () -> Unit): EditNoteBottomSheetFragment {
        this.deleteClickHandler = deleteClickHandler
        return this
    }

    fun setLabelListener(labelClickHandler: () -> Unit): EditNoteBottomSheetFragment {
        this.labelClickHandler = labelClickHandler
        return this
    }

    fun setPinListener(pinClickHandler: () -> Unit): EditNoteBottomSheetFragment {
        this.pinClickHandler = pinClickHandler
        return this
    }

    fun setArchiveListener(archiveClickHandler: () -> Unit): EditNoteBottomSheetFragment {
        this.archiveClickHandler = archiveClickHandler
        return this
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}