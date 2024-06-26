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

    @Query("SELECT pinned FROM notes WHERE noteId = :noteId")
    fun getPinState(noteId: Long): Boolean

    /**
     * Bổ sung vào ViewModel
     * */
        @Query("UPDATE notes SET archived = 1 WHERE noteId = :noteId")
        suspend fun archive(noteId: Long)

        @Query("UPDATE notes SET archived = 1 WHERE noteId IN (:noteIds)")
        suspend fun archiveAll(noteIds: List<Long>)

        @Query("UPDATE notes SET archived = 0 WHERE noteId = :noteId")
        suspend fun unArchive(noteId: Long)

        @Query("UPDATE notes SET archived = 0 WHERE noteId IN (:noteIds)")
        suspend fun unArchiveAll(noteIds: List<Long>)

        @Query("UPDATE notes SET deleted = 1 WHERE noteId = :noteId")
        suspend fun moveToTrash(noteId: Long)

        @Query("UPDATE notes SET deleted = 1 WHERE noteId IN (:noteIds)")
        suspend fun moveAllToTrash(noteIds: List<Long>)

        @Query("UPDATE notes SET deleted = 0 WHERE noteId = :noteId")
        suspend fun removeFromTrash(noteId: Long)

        @Query("UPDATE notes SET deleted = 0 WHERE noteId IN (:noteIds)")
        suspend fun removeAllFromTrash(noteIds: List<Long>)

        @Query("UPDATE notes SET pinned = 1 WHERE noteId = :noteId")
        suspend fun pin(noteId: Long)

        @Query("UPDATE notes SET pinned = 1 WHERE noteId IN (:noteIds)")
        suspend fun pinAll(noteIds: List<Long>)

        @Query("UPDATE notes SET pinned = 0 WHERE noteId = :noteId")
        suspend fun unPin(noteId: Long)

        @Query("UPDATE notes SET pinned = 0 WHERE noteId IN (:noteIds)")
        suspend fun unPinAll(noteIds: List<Long>)
    /***/

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addLabelToNote(crossRef: NoteLabelCrossRef)

    @Query("DELETE FROM notes_n_labels WHERE noteId = :noteId AND labelId = :labelId")
    suspend fun removeLabelFromNote(noteId: Long, labelId: Long)

    @Query("DELETE FROM notes WHERE noteId = :noteId")
    suspend fun deleteNoteById(noteId: Long)

    @Query("DELETE FROM notes WHERE noteId IN (:noteIds)")
    suspend fun deleteAllNoteByIds(noteIds: List<Long>)

    @Query("DELETE FROM notes_n_labels WHERE noteId IN (" +
            "SELECT noteId FROM notes " +
            "WHERE title = '' " +
            "AND content = ''" +
            "AND (SELECT COUNT (*) FROM images WHERE noteId = notes.noteId) = 0)")
    suspend fun deleteLabelOfEmptyNotes()

    @Query("DELETE FROM notes " +
            "WHERE title = '' " +
            "AND content = '' " +
            "AND (SELECT COUNT (*) FROM images WHERE noteId = notes.noteId) = 0")
    suspend fun deleteEmptyNotes()

    @Query("DELETE FROM notes_n_labels WHERE noteId = :noteId")
    suspend fun removeNoteLabels(noteId: Long)

    @Query("DELETE FROM notes_n_labels WHERE noteId IN (:noteIds)")
    suspend fun removeAllNoteLabels(noteIds: List<Long>)

    @Transaction
    @Query("SELECT * FROM notes WHERE deleted = 0 AND archived = 0 ORDER BY pinned DESC, dateCreated DESC")
    fun getNotesWithLabels(): LiveData<List<NoteWithLabels>>

    @Transaction
    @Query("SELECT * FROM notes WHERE deleted = 1 ORDER BY dateCreated DESC")
    fun getDeletedNotesWithLabels(): LiveData<List<NoteWithLabels>>

    @Transaction
    @Query("SELECT * FROM notes WHERE archived = 1 ORDER BY dateCreated DESC")
    fun getArchivedNotesWithLabels(): LiveData<List<NoteWithLabels>>

    @Transaction
    @Query("SELECT * FROM notes WHERE noteId = :noteId")
    fun getNoteWithLabels(noteId: Long): LiveData<NoteWithLabels>

    @Transaction
    @Query("SELECT * FROM notes WHERE noteId IN (SELECT noteId FROM notes_n_labels WHERE labelId = :labelId)")
    fun getNotesByLabelId(labelId: Long): LiveData<List<NoteWithLabels>>
}