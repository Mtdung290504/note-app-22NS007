package com.example.noteapp_22ns007.model.database.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.noteapp_22ns007.model.database.entities.Note
import com.example.noteapp_22ns007.model.database.entities.NoteLabelCrossRef
import com.example.noteapp_22ns007.model.database.entities.relationshipClasses.NoteWithLabels

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addLabelToNote(crossRef: NoteLabelCrossRef)

    @Query("DELETE FROM notes_n_labels WHERE noteId = :noteId AND labelId = :labelId")
    suspend fun removeLabelFromNote(noteId: Long, labelId: Long)

    @Query("DELETE FROM notes WHERE noteId = :noteId")
    suspend fun deleteNoteById(noteId: Long)

    @Query("DELETE FROM notes_n_labels WHERE noteId IN (SELECT noteId FROM notes WHERE title = '' AND content = '')")
    suspend fun deleteLabelOfEmptyNotes()

    @Query("DELETE FROM notes WHERE title = '' AND content = ''")
    suspend fun deleteEmptyNotes()

    @Query("DELETE FROM notes_n_labels WHERE noteId = :noteId")
    suspend fun removeNoteLabels(noteId: Long)

    @Transaction
    @Query("SELECT * FROM notes WHERE pinned = 0 AND deleted = 0 AND archived = 0 ORDER BY dateCreated DESC")
    fun getNotesWithLabels(): LiveData<List<NoteWithLabels>>

    @Transaction
    @Query("SELECT * FROM notes WHERE noteId = :noteId ORDER BY dateCreated DESC")
    fun getNoteWithLabels(noteId: Long): LiveData<NoteWithLabels>

    @Transaction
    @Query("SELECT * FROM notes WHERE noteId IN (SELECT noteId FROM notes_n_labels WHERE labelId = :labelId)")
    fun getNotesByLabelId(labelId: Long): LiveData<List<NoteWithLabels>>
}