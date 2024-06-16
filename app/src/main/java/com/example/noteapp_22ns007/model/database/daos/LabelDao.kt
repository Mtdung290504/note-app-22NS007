package com.example.noteapp_22ns007.model.database.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.noteapp_22ns007.model.database.entities.Label

@Dao
interface LabelDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(label: Label): Long

    @Update
    suspend fun update(label: Label)

    @Query("DELETE FROM labels WHERE labelId = :labelId")
    suspend fun deleteLabelById(labelId: Long)

    @Query("DELETE FROM notes_n_labels WHERE labelId = :labelId")
    suspend fun removeLabelNotes(labelId: Long)

    @Query("SELECT * FROM labels")
    fun getAllLabels(): LiveData<List<Label>>

    @Transaction
    @Query("SELECT * FROM labels WHERE labelId IN (SELECT labelId FROM notes_n_labels WHERE noteId = :noteId)")
    fun getLabelsByNoteId(noteId: Long): LiveData<List<Label>>
}