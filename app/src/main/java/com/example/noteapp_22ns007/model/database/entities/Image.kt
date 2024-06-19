package com.example.noteapp_22ns007.model.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images")
data class Image (
    @PrimaryKey
    val imageId: Long? = 0,
    val noteId: Long,
    val image: String
)