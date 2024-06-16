package com.example.noteapp_22ns007.model.database.entities.relationshipClasses

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.noteapp_22ns007.model.database.entities.Label
import com.example.noteapp_22ns007.model.database.entities.Note
import com.example.noteapp_22ns007.model.database.entities.NoteLabelCrossRef

data class LabelWithNotes(
    @Embedded val label: Label,
    @Relation(
        parentColumn = "labelId",
        entityColumn = "noteId",
        associateBy = Junction(NoteLabelCrossRef::class)
    )
    val notes: List<Note>
)