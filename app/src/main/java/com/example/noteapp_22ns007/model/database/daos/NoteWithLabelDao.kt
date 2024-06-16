package com.example.noteapp_22ns007.model.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.noteapp_22ns007.model.database.entities.relationshipClasses.LabelWithNotes
import com.example.noteapp_22ns007.model.database.entities.relationshipClasses.NoteWithLabels
import com.example.noteapp_22ns007.model.database.entities.NoteLabelCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteWithLabelDao {
    @Insert
    suspend fun insert(noteLabelCrossRef: NoteLabelCrossRef)

//    @Transaction
//    @Query("SELECT * FROM notes WHERE noteId = :noteId")
//    fun getLabelsForNoteId(noteId: Int): List<NoteWithLabels>
//
//    @Transaction
//    @Query("SELECT * FROM labels WHERE labelId = :labelId")
//    fun getNotesForLabelId(labelId: Int): List<LabelWithNotes>

    @Delete
    fun deleteNoteLabelCrossRef(noteLabelCrossRef: NoteLabelCrossRef)

    @Query("DELETE FROM notes_n_labels WHERE noteId = :noteId")
    fun deleteLabelsForNoteId(noteId: Int)

    @Query("DELETE FROM notes_n_labels WHERE labelId = :labelId")
    fun deleteNotesForLabelId(labelId: Int)
}