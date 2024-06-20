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
import com.example.noteapp_22ns007.adapters.ChooseLabelAdapter
import com.example.noteapp_22ns007.adapters.ImageAdapter
import com.example.noteapp_22ns007.adapters.LabelAdapter
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
    private var pinState: Boolean = false

    // Image adapter
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var imageLiveData: LiveData<List<Image>>
    private var images: List<Image> = listOf()

    // Label adapter and Choose label adapter
    private lateinit var labelAdapter: LabelAdapter
    private lateinit var checkedLabelLiveData: LiveData<List<Label>>
    private var checkedLabels: List<Label> = listOf()

    // Sub Fragments
    private lateinit var editNoteBottomSheetFragment: EditNoteBottomSheetFragment
    private lateinit var chooseLabelBottomSheetFragment: ChooseLabelBottomSheetFragment

    // Another
    private lateinit var currentPhotoPath: String

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            noteId = it.getLong(NOTE_ID)
            title = it.getString(TITLE)
            content = it.getString(CONTENT) ?: ""
            dateCreated = it.getSerializable(DATE_CREATED) as Date
            pinState = it.getBoolean(PIN_STATE)
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
            dateCreated = dateCreated,
            pinned = pinState
        )

        mainActivity.noteViewModel.update(updatedNote)
    }

    private fun initializeViewElements(binding: FragmentEditNoteBinding) {
        binding.noteTitle.setText(title)
        binding.noteContent.setText(content)

        binding.btnBack.setOnClickListener {
            saveDataAndHide()
        }

        // Initialize RecyclerView and observe for images
        imageAdapter = ImageAdapter(images, object: ImageAdapter.OnImageLongClickListener {
            override fun onImageLongClick(image: Image) {
                AlertDialog.Builder(context).apply {
                    setTitle("Xác nhận xóa ảnh")
                    setMessage("\nBạn có chắc chắn muốn xóa ảnh này không?")
                    setPositiveButton("Xóa") { _, _ ->
                        val path = image.image
                        val file = File(path)
                        if(file.exists()) {
                            Log.d("DELETE 1 IMG", "Deleted file: $path")
                            file.delete()
                        } else {
                            Log.d("DELETE 1 IMG", "File not found!: $path")
                        }
                        mainActivity.imageViewModel.delete(image)
                    }
                    setNegativeButton("Hủy") { dialog, _ ->
                        dialog.dismiss()
                    }
                    create()
                    show()
                }
            }
        })
        binding.imageRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = imageAdapter
        }
        checkImageContainer()

        // Observe images
        imageLiveData = mainActivity.imageViewModel.getImageOfNote(noteId)
        imageLiveData.observe(viewLifecycleOwner) { imagesOfNote ->
            images = imagesOfNote
            imageAdapter.updateImage(images)
            checkImageContainer()
        }

        // Initialize RecyclerView and observe for labels of this note
        labelAdapter = LabelAdapter(checkedLabels, mainActivity, false)
        binding.labelRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = labelAdapter
        }
        checkLabelContainer()

        // Observe labels of this note
        checkedLabelLiveData = mainActivity.labelViewModel.getLabelsByNoteId(noteId)
        checkedLabelLiveData.observe(viewLifecycleOwner) { labelOfNote ->
            checkedLabels = labelOfNote
            labelAdapter.updateLabel(checkedLabels)
            checkLabelContainer()
        }

        // Initialize sub fragments
        chooseLabelBottomSheetFragment = ChooseLabelBottomSheetFragment.newInstance(noteId)
        Log.d("DEBUG Pin state", pinState.toString())
        editNoteBottomSheetFragment = EditNoteBottomSheetFragment()
            .setPinState(pinState)
            .setDeleteListener {
                // Real delete
                /*
                AlertDialog.Builder(context).apply {
                    setTitle("Xác nhận xóa ghi chú")
                    setMessage("\nBạn có chắc chắn muốn xóa ghi chú này không?")
                    setPositiveButton("Xóa") { _, _ ->
                        val noteTitle = binding.noteTitle.text

                        mainActivity.noteViewModel.deleteNoteById(noteId)
                        lifecycleScope.launch(Dispatchers.IO) {
                            imageAdapter.images.forEach{
                                val path = it.image
                                val file = File(path)
                                if(file.exists()) {
                                    Log.d("DELETE IMG", "Deleted file: $path")
                                    file.delete()
                                } else {
                                    Log.d("DELETE IMG", "File not found!: $path")
                                }
                            }
                        }
                        mainActivity.imageViewModel.deleteImageOfNote(noteId)

                        binding.noteTitle.text = null
                        binding.noteContent.text = null
                        editNoteBottomSheetFragment.dismiss()

                        mainActivity.hideEditNoteFragment(this@EditNoteFragment)
                        Utils.notification(view, "Đã xóa note \"$noteTitle\"", "") {}
                    }
                    setNegativeButton("Hủy") { dialog, _ ->
                        dialog.dismiss()
                    }
                    create()
                    show()
                }*/

                mainActivity.noteViewModel.moveToTrash(noteId)
                Utils.notification(requireView(), "Đã chuyển ghi chú \"${title}\" vào thùng rác", "") {}
                editNoteBottomSheetFragment.dismiss()
                mainActivity.hideEditNoteFragment(this)
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
                @Suppress("DEPRECATION")
                chooseLabelBottomSheetFragment.show(requireFragmentManager(), chooseLabelBottomSheetFragment.tag)
                editNoteBottomSheetFragment.dismiss()
            }
            .setPinListener {
                if(noteIsEmpty()) {
                    Utils.notification(editNoteBottomSheetFragment.requireView(), "Ghi chú trống!", "") {}
                    return@setPinListener
                }

                if(!editNoteBottomSheetFragment.pinState) {
                    mainActivity.noteViewModel.pin(noteId)
                    pinState = true
                    editNoteBottomSheetFragment.setPinState(true)
                    Utils.notification(view, "Đã ghim ghi chú \"${title}\"", "") {}
                } else {
                    mainActivity.noteViewModel.pin(noteId)
                    pinState = false
                    editNoteBottomSheetFragment.setPinState(false)
                    Utils.notification(view, "Đã bỏ ghim ghi chú \"${title}\"", "") {}
                }

                editNoteBottomSheetFragment.dismiss()
            }
            .setArchiveListener {
                if(noteIsEmpty()) {
                    Utils.notification(editNoteBottomSheetFragment.requireView(), "Ghi chú trống!", "") {}
                    return@setArchiveListener
                }

                mainActivity.noteViewModel.archive(noteId)
                Utils.notification(view, "Đã lưu trữ ghi chú \"${title}\"", "") {}
                editNoteBottomSheetFragment.dismiss()
            }
        binding.moreOptions.setOnClickListener {
            @Suppress("DEPRECATION")
            editNoteBottomSheetFragment.show(requireFragmentManager(), editNoteBottomSheetFragment.tag)
        }

        setupSaveActionForEditText(binding.noteTitle)
        setupSaveActionForEditText(binding.noteContent)

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
            binding.labelForImage.visibility = View.GONE
            binding.imageRecyclerView.visibility = View.GONE
        } else {
            binding.labelForImage.visibility = View.VISIBLE
            binding.imageRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun checkLabelContainer() {
        if(labelAdapter.itemCount == 0) {
            binding.labelForLabel.visibility = View.GONE
            binding.labelRecyclerView.visibility = View.GONE
        } else {
            binding.labelForLabel.visibility = View.VISIBLE
            binding.labelRecyclerView.visibility = View.VISIBLE
        }
    }

    fun saveDataAndHide() {
        if (noteIsEmpty()) {
            mainActivity.noteViewModel.deleteNoteById(noteId)
            Utils.notification(view, "Đã xóa ghi chú trống", "") {}
        } else {
            saveData()
        }

        mainActivity.hideEditNoteFragment(this)
    }

    private fun noteIsEmpty(): Boolean {
        val noteTitle = binding.noteTitle.text.toString().trim()
        val noteContent = binding.noteContent.text.toString().trim()
        val imageCount = imageAdapter.itemCount

        return noteTitle.isBlank() && noteContent.isBlank() && imageCount == 0
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

        return when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
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
        private const val PIN_STATE = "pinState"

        @JvmStatic
        fun newInstance(noteId: Long, title: String, content: String, dateCreated: Date, pinState: Boolean): EditNoteFragment {
            return EditNoteFragment().apply {
                arguments = Bundle().apply {
                    putLong(NOTE_ID, noteId)
                    putString(TITLE, title)
                    putString(CONTENT, content)
                    putSerializable(DATE_CREATED, dateCreated)
                    putBoolean(PIN_STATE, pinState)
                }
            }
        }
    }
}