package com.example.noteapp_22ns007.model.database.entities.relationshipClasses

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.noteapp_22ns007.model.database.entities.Label
import com.example.noteapp_22ns007.model.database.entities.Note
import com.example.noteapp_22ns007.model.database.entities.NoteLabelCrossRef

data class NoteWithLabels(
    @Embedded val note: Note,
    @Relation(
        parentColumn = "noteId",
        entityColumn = "labelId",
        associateBy = Junction(NoteLabelCrossRef::class)
    )
    val labels: List<Label>
)