package com.example.noteapp_22ns007.model.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "labels",
    indices = [Index(value = ["name"], unique = true)]
)
data class Label(
    @PrimaryKey(autoGenerate = true)
    val labelId: Int = 0,
    val name: String
)