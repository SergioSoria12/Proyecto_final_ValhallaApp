package edu.sergiosoria.valhallathebox.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "class_sessions")
data class ClassSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val dateTime: String,
    val assignedTo: String
)