package com.example.noteapp_22ns007.model.database.entities

import androidx.room.Entity

@Entity(tableName = "notes_n_labels", primaryKeys = ["noteId", "labelId"])
data class NoteLabelCrossRef(
    val noteId: Int,
    val labelId: Int
)