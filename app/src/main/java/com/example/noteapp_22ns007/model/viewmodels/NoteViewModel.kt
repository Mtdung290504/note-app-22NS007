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
            noteDao.unPin(noteId)
            noteDao.archive(noteId)
        }
    }

    fun archiveAll(noteIds: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.unPinAll(noteIds)
            noteDao.archiveAll(noteIds)
        }
    }

    fun unArchive(noteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.unArchive(noteId)
        }
    }

    fun unArchiveAll(noteIds: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.unArchiveAll(noteIds)
        }
    }

    fun moveToTrash(noteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.unPin(noteId)
            noteDao.moveToTrash(noteId)
            if(noteId == insertedNoteId.value)
                _insertedNoteId.postValue(-1)
        }
    }

    fun moveAllToTrash(noteIds: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.unPinAll(noteIds)
            noteDao.moveAllToTrash(noteIds)
        }
    }

    fun removeFromTrash(noteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.removeFromTrash(noteId)
        }
    }

    fun removeAllFromTrash(noteIds: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.removeAllFromTrash(noteIds)
        }
    }

    fun pin(noteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.pin(noteId)
        }
    }

    fun pinAll(noteIds: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.pinAll(noteIds)
        }
    }

    fun unPin(noteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.unPin(noteId)
        }
    }

    fun unPinAll(noteIds: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.unPinAll(noteIds)
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

    fun deleteAllNoteByIds(noteIds: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            safeDeleteAllNoteByIds(noteIds)
        }
    }
    private suspend fun safeDeleteAllNoteByIds(noteIds: List<Long>) {
        noteDao.removeAllNoteLabels(noteIds)
        noteDao.deleteAllNoteByIds(noteIds)
    }

    fun getNotesWithLabels() = noteDao.getNotesWithLabels()

    fun getDeletedNoteWithLabels() = noteDao.getDeletedNotesWithLabels()

    fun getArchivedNoteWithLabels() = noteDao.getArchivedNotesWithLabels()

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