package com.example.noteapp_22ns007.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.noteapp_22ns007.model.database.daos.LabelDao
import com.example.noteapp_22ns007.model.database.daos.NoteDao
import com.example.noteapp_22ns007.model.database.daos.NoteWithLabelDao
import com.example.noteapp_22ns007.model.database.entities.Label
import com.example.noteapp_22ns007.model.database.entities.Note
import com.example.noteapp_22ns007.model.database.entities.NoteLabelCrossRef

@Database(
    entities = [Note::class, Label::class, NoteLabelCrossRef::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun labelDao(): LabelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "NoteDatabase"
                ).fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                return instance
            }
        }
    }
}