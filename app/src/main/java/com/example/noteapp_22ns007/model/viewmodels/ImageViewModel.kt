package com.example.noteapp_22ns007.model.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.noteapp_22ns007.model.database.daos.ImageDao
import com.example.noteapp_22ns007.model.database.entities.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImageViewModel(private val imageDao: ImageDao) : ViewModel() {

    fun insert(image: Image) {
        viewModelScope.launch(Dispatchers.IO) {
            imageDao.insert(image)
        }
    }

    fun delete(image: Image) {
        viewModelScope.launch(Dispatchers.IO) {
            imageDao.delete(image)
        }
    }

    fun getImageOfNote(noteId: Long) = imageDao.getImageOfNote(noteId)

    fun deleteImageOfNote(noteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            imageDao.deleteImageOfNote(noteId)
        }
    }

    class ImageViewModelFactory(private val imageDao: ImageDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ImageViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ImageViewModel(imageDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}