package com.example.noteapp_22ns007.model.database.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.noteapp_22ns007.model.database.entities.Image

@Dao
interface ImageDao {
    @Insert
    suspend fun insert(image: Image): Long

    @Delete
    suspend fun delete(image: Image): Int

    @Query("SELECT * FROM images WHERE noteId = :noteId")
    fun getImageOfNote(noteId: Long): LiveData<List<Image>>

    @Query("SELECT * FROM images WHERE noteId IN (:noteIds)")
    fun getImageOfAllNotes(noteIds: List<Long>): LiveData<List<Image>>

    @Query("DELETE FROM images WHERE noteId = :noteId")
    suspend fun deleteImageOfNote(noteId: Long)

    @Query("DELETE FROM images WHERE noteId IN (:noteIds)")
    suspend fun deleteImageOfAllNote(noteIds: List<Long>)
}