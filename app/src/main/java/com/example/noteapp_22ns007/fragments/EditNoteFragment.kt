package com.example.noteapp_22ns007.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
//noinspection ExifInterface
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp_22ns007.MainActivity
import com.example.noteapp_22ns007.Utils
import com.example.noteapp_22ns007.adapters.ImageAdapter
import com.example.noteapp_22ns007.databinding.FragmentEditNoteBinding
import com.example.noteapp_22ns007.model.database.Converters
import com.example.noteapp_22ns007.model.database.entities.Image
import com.example.noteapp_22ns007.model.database.entities.Label
import com.example.noteapp_22ns007.model.database.entities.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    private lateinit var imageLiveData: LiveData<List<Image>>
    private var images: List<Image> = listOf()
    private val converters = Converters()

    private lateinit var editNoteBottomSheetFragment: EditNoteBottomSheetFragment

    private lateinit var currentPhotoPath: String

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

        savedInstanceState?.let {
            currentPhotoPath = it.getString(KEY_CURRENT_PHOTO_PATH).toString()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Lưu giá trị của currentPhotoPath vào Bundle
        outState.putString(KEY_CURRENT_PHOTO_PATH, currentPhotoPath)
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
        val updatedNote = Note(
            noteId = noteId,
            title = noteTitle,
            content = noteContent,
            dateCreated = dateCreated
        )

        mainActivity.noteViewModel.update(updatedNote)
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

        imageLiveData = mainActivity.imageViewModel.getImageOfNote(noteId)
        imageLiveData.observe(viewLifecycleOwner) { imagesOfNote ->
            images = imagesOfNote
            imageAdapter.updateImage(images)
            checkImageContainer()
        }

        editNoteBottomSheetFragment = EditNoteBottomSheetFragment()
            .setDeleteListener {
                AlertDialog.Builder(context).apply {
                    setTitle("Xác nhận xóa ghi chú")
                    setMessage("\nBạn có chắc chắn muốn xóa ghi chú này không?")
                    setPositiveButton("Xóa") { _, _ ->
                        val noteTitle = binding.noteTitle.text

                        mainActivity.noteViewModel.deleteNoteById(noteId)
                        mainActivity.imageViewModel.deleteImageOfNote(noteId)

                        binding.noteTitle.text = null
                        binding.noteContent.text = null
                        editNoteBottomSheetFragment.dismiss()

                        mainActivity.hideEditNoteFragment(this@EditNoteFragment)
                        Utils.notification(view, "Đã xóa note \"$noteTitle\"") {}
                    }
                    setNegativeButton("Hủy") { dialog, _ ->
                        dialog.dismiss()
                    }
                    create()
                    show()
                }
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
            .setLabelListener {
                Toast.makeText(context, "Edit label for note: $noteId", Toast.LENGTH_SHORT).show()
            }
            .setPinListener {
                Toast.makeText(context, "Pinned note: $noteId", Toast.LENGTH_SHORT).show()
            }
            .setArchiveListener {
                Toast.makeText(context, "Archived note: $noteId", Toast.LENGTH_SHORT).show()
            }

        setupSaveActionForEditText(binding.noteTitle)
        setupSaveActionForEditText(binding.noteContent)
        binding.moreOptions.setOnClickListener {
            @Suppress("DEPRECATION")
            editNoteBottomSheetFragment.show(requireFragmentManager(), editNoteBottomSheetFragment.tag)
        }

        binding.fabSave.setOnClickListener {
            saveDataAndHide()
        }
    }

    private fun setupSaveActionForEditText(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && (s.endsWith(" ") || s.endsWith("\n"))) {
                    saveData()
                }
            }

            override fun afterTextChanged(s: Editable?) { }
        })

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveData()
            }
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
        val noteTitle = binding.noteTitle.text.toString().trim()
        val noteContent = binding.noteContent.text.toString().trim()

        if (noteTitle.isBlank() && noteContent.isBlank()) {
            mainActivity.noteViewModel.deleteNoteById(noteId)
            Toast.makeText(context, "Đã xóa ghi chú trống", Toast.LENGTH_SHORT).show()
        } else {
            saveData()
        }

        mainActivity.hideEditNoteFragment(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val cameraResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Giải nén ảnh từ file
            val photoFile = File(currentPhotoPath)

            if (photoFile.exists()) {
                val photo = BitmapFactory.decodeFile(photoFile.absolutePath)
                val imagePath = saveImageToStorage(photo)

                // Xử lý ảnh trong background thread
                lifecycleScope.launch(Dispatchers.IO) {
                    mainActivity.imageViewModel.insert(Image(null, noteId, imagePath))
                }

                photoFile.delete()
            }
        }
    }

    private fun openCamera() {
        // Tạo file tạm thời để lưu ảnh
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            null
        }

        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.noteapp_22ns007.FileProvider",
                it
            )
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            }
            cameraResultLauncher.launch(cameraIntent)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Tạo một tên file độc nhất
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Lưu lại đường dẫn file
            currentPhotoPath = absolutePath
        }
    }

    private fun saveImageToStorage(bitmap: Bitmap): String {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val imageFile = File(storageDir, "JPEG_${timeStamp}.jpg")

//        currentPhotoPath = imageFile.absolutePath
//        val rotatedBitmap = rotateImage(bitmap, 90)
        val rotatedBitmap = rotateImageIfRequired(bitmap, currentPhotoPath)
        val fos = FileOutputStream(imageFile)
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.close()

        return imageFile.absolutePath
    }

    private fun rotateImageIfRequired(bitmap: Bitmap, imagePath: String?): Bitmap {
        imagePath ?: return bitmap

        val ei = ExifInterface(imagePath)
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270)
            else -> bitmap
        }
    }

    private fun rotateImage(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // Xử lý kết quả yêu cầu quyền từ người dùng
    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Quyền đã được cấp, mở camera
                openCamera()
            } else {
                // Quyền bị từ chối, bạn có thể thông báo cho người dùng biết
                Toast.makeText(
                    requireContext(),
                    "Camera and storage permissions denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    companion object {
        private const val KEY_CURRENT_PHOTO_PATH = "currentPhotoPath"
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