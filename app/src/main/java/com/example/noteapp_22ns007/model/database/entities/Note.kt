package com.example.noteapp_22ns007.model.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val noteId: Long? = 0,
    val title: String,
    val content: String,
    val dateCreated: Date,
    val archived: Boolean? = false,
    val pinned: Boolean? = false,
    val deleted: Boolean? = false,
    val images: List<ByteArray>? = emptyList()
)