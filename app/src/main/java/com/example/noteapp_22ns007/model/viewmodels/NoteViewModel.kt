package com.example.noteapp_22ns007.model.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.noteapp_22ns007.model.database.daos.NoteDao
import com.example.noteapp_22ns007.model.database.entities.Note
import com.example.noteapp_22ns007.model.database.entities.NoteLabelCrossRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(private val noteDao: NoteDao) : ViewModel() {
    private val _insertedNoteId = MutableLiveData<Long>(-1)

    val insertedNoteId: LiveData<Long>
        get() = _insertedNoteId

    fun insert(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            val insertedId = noteDao.insert(note)
            _insertedNoteId.postValue(insertedId)
        }
    }

    fun update(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.update(note)
            if(note.noteId == insertedNoteId.value)
                _insertedNoteId.postValue(-1)
        }
    }

    fun archive(noteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.archive(noteId)
        }
    }

    fun unArchive(noteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.unArchive(noteId)
        }
    }

    fun moveToTrash(noteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.moveToTrash(noteId)
            if(noteId == insertedNoteId.value)
                _insertedNoteId.postValue(-1)
        }
    }

    fun removeFromTrash(noteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.removeFromTrash(noteId)
        }
    }

    fun pin(noteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.pin(noteId)
        }
    }

    fun unPin(noteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.unPin(noteId)
        }
    }

    fun addLabelToNote(crossRef: NoteLabelCrossRef) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.addLabelToNote(crossRef)
        }
    }

    fun removeLabelFromNote(noteId: Long, labelId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.removeLabelFromNote(noteId, labelId)
        }
    }

    fun deleteEmptyNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            safeDeleteAllEmptyNotes()
        }
    }
    private suspend fun safeDeleteAllEmptyNotes() {
        noteDao.deleteLabelOfEmptyNotes()
        noteDao.deleteEmptyNotes()
    }

    fun deleteNoteById(noteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            safeDeleteNoteById(noteId)
            if(noteId == insertedNoteId.value)
                _insertedNoteId.postValue(-1)
        }
    }
    private suspend fun safeDeleteNoteById(noteId: Long) {
        noteDao.removeNoteLabels(noteId) // Xóa các quan hệ
        noteDao.deleteNoteById(noteId) // Sau đó xóa note
    }

    fun getNotesWithLabels() = noteDao.getNotesWithLabels()

    fun getNotesByLabelId(labelId: Long) = noteDao.getNotesByLabelId(labelId)

    fun getNoteById(labelId: Long) = noteDao.getNoteWithLabels(labelId)

    class NoteViewModelFactory(private val noteDao: NoteDao) : ViewModelProvider.Factory {
        override fun <T: ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NoteViewModel(noteDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}