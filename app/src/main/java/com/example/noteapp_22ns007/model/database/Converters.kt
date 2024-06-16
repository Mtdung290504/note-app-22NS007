package com.example.noteapp_22ns007.model.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

    @TypeConverter
    fun fromByteArrayList(images: List<ByteArray>?): String? {
        return gson.toJson(images)
    }

    @TypeConverter
    fun toByteArrayList(imagesString: String?): List<ByteArray>? {
        if (imagesString == null) {
            return null
        }
        val listType = object : TypeToken<List<ByteArray>>() {}.type
        return gson.fromJson(imagesString, listType)
    }
}