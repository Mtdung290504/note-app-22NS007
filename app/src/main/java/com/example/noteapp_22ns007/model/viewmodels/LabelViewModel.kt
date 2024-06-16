package com.example.noteapp_22ns007.model.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.noteapp_22ns007.model.database.daos.LabelDao
import com.example.noteapp_22ns007.model.database.entities.Label
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LabelViewModel(private val labelDao: LabelDao) : ViewModel() {
    private var _insertedLabelId: Long = -1
    private val _insertResult = MutableLiveData<Boolean>()

    val insertedLabelId: Long
        get() = _insertedLabelId
    val insertResult: LiveData<Boolean>
        get() = _insertResult

    fun insert(label: Label) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = labelDao.insert(label)
            _insertedLabelId = result
            _insertResult.postValue(result != -1L)
        }
    }

    fun update(label: Label) {
        viewModelScope.launch(Dispatchers.IO) {
            labelDao.update(label)
        }
    }

    fun deleteLabelById(labelId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            safeDeleteLabelById(labelId)
        }
    }
    private suspend fun safeDeleteLabelById(labelId: Long) {
        labelDao.removeLabelNotes(labelId) // Xóa các quan hệ
        labelDao.deleteLabelById(labelId) // Sau đó xóa label
    }

    fun getAllLabels() = labelDao.getAllLabels()

    fun getLabelsByNoteId(noteId: Long) = labelDao.getLabelsByNoteId(noteId)

    class LabelViewModelFactory(private val labelDao: LabelDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LabelViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LabelViewModel(labelDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}