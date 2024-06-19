package com.example.noteapp_22ns007.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.noteapp_22ns007.MainActivity
import android.Manifest
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp_22ns007.adapters.ImageAdapter
import com.example.noteapp_22ns007.databinding.FragmentEditNoteBinding
import com.example.noteapp_22ns007.model.database.entities.Label
import com.example.noteapp_22ns007.model.database.entities.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class EditNoteFragment : Fragment() {
    private lateinit var mainActivity: MainActivity
    private var _binding: FragmentEditNoteBinding? = null
    private val binding get() = _binding!!

    // Data
    private var noteId: Long = 0
    private var title: String? = null
    private lateinit var content: String
    private lateinit var dateCreated: Date
    private lateinit var labels: List<Label>
    private lateinit var imageAdapter: ImageAdapter
    private val images: MutableList<Bitmap> = mutableListOf()

    private lateinit var editNoteBottomSheetFragment: EditNoteBottomSheetFragment

    @Suppress("DEPRECATION", "CAST_NEVER_SUCCEEDS")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            noteId = bundle.getLong(NOTE_ID)
            title = bundle.getString(TITLE)
            content = bundle.getString(CONTENT) ?: ""
            dateCreated = bundle.getSerializable(DATE_CREATED) as Date
            val parcelableLabels: ArrayList<Parcelable>? = bundle.getParcelableArrayList(LABELS)
            labels = parcelableLabels?.mapNotNull { it as? Label } ?: emptyList()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditNoteBinding.inflate(inflater, container, false)
        mainActivity = activity as MainActivity

        initializeViewElements(binding)

        return binding.root
    }

    private fun saveData() {
        val noteTitle = binding.noteTitle.text.toString().trim()
        val noteContent = binding.noteContent.text.toString().trim()

        if (noteTitle.isBlank() && noteContent.isBlank()) {
            mainActivity.noteViewModel.deleteNoteById(noteId)
            Toast.makeText(context, "Đã xóa ghi chú trống", Toast.LENGTH_SHORT).show()
        } else {
            val updatedNote = Note(
                noteId = noteId,
                title = noteTitle,
                content = noteContent,
                dateCreated = dateCreated
            )
            mainActivity.noteViewModel.update(updatedNote)
        }
    }

    private fun initializeViewElements(binding: FragmentEditNoteBinding) {
        binding.noteTitle.setText(title)
        binding.noteContent.setText(content)

        binding.btnBack.setOnClickListener {
            saveDataAndHide()
        }

        // Initialize RecyclerView for images
        imageAdapter = ImageAdapter(images)
        binding.imageRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }
        checkImageContainer()

        editNoteBottomSheetFragment = EditNoteBottomSheetFragment()
            .setDeleteListener {
                val noteTitle = binding.noteTitle.text
                mainActivity.noteViewModel.deleteNoteById(noteId)
                binding.noteTitle.text = null
                binding.noteContent.text = null
                editNoteBottomSheetFragment.dismiss()
                mainActivity.hideEditNoteFragment(this)
                Toast.makeText(context, "Deleted note::$noteId: \"$noteTitle\"", Toast.LENGTH_SHORT).show()
            }
            .setTakePictureListener {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    ActivityCompat.requestPermissions(requireActivity(),
                        arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
                }
            }
            .setGetPictureListener {
                Toast.makeText(context, "Get picture for note: $noteId", Toast.LENGTH_SHORT).show()
            }
            .setLabelListener {
                Toast.makeText(context, "Edit label for note: $noteId", Toast.LENGTH_SHORT).show()
            }
            .setPinListener {
                Toast.makeText(context, "Pinned note: $noteId", Toast.LENGTH_SHORT).show()
            }
            .setArchiveListener {
                Toast.makeText(context, "Archived note: $noteId", Toast.LENGTH_SHORT).show()
            }

        binding.moreOptions.setOnClickListener {
            @Suppress("DEPRECATION")
            editNoteBottomSheetFragment.show(requireFragmentManager(), editNoteBottomSheetFragment.tag)
        }

        binding.fabSave.setOnClickListener {
            saveDataAndHide()
        }
    }

    private fun checkImageContainer() {
        if(imageAdapter.itemCount == 0) {
            binding.imageRecyclerView.visibility = View.GONE
        } else {
            binding.imageRecyclerView.visibility = View.VISIBLE
        }
    }

    fun saveDataAndHide() {
        saveData()
        mainActivity.hideEditNoteFragment(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Suppress("DEPRECATION")
    private val cameraResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val photo: Bitmap = data?.extras?.get("data") as Bitmap

            // Xử lý ảnh trong background thread
            lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    imageAdapter.addImage(photo)
                    checkImageContainer()
                }
            }
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraResultLauncher.launch(cameraIntent)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission is needed to take photos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val NOTE_ID = "noteId"
        private const val TITLE = "title"
        private const val CONTENT = "content"
        private const val DATE_CREATED = "dateCreated"
        private const val LABELS = "labels"

        @Suppress("CAST_NEVER_SUCCEEDS")
        @JvmStatic
        fun newInstance(noteId: Long, title: String, content: String, dateCreated: Date, labels: List<Label>): EditNoteFragment {
            return EditNoteFragment().apply {
                arguments = Bundle().apply {
                    putLong(NOTE_ID, noteId)
                    putString(TITLE, title)
                    putString(CONTENT, content)
                    putSerializable(DATE_CREATED, dateCreated)
                    val parcelableLabels = labels.map { it as Parcelable }
                    putParcelableArrayList(LABELS, ArrayList(parcelableLabels))
                }
            }
        }
    }
}