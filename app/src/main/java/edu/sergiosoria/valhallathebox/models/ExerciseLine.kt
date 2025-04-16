package edu.sergiosoria.valhallathebox.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = WodBlock::class,
        parentColumns = ["blockId"],
        childColumns = ["blockOwnerId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ExerciseLine(
    @PrimaryKey(autoGenerate = true) var exerciseId: Long = 0,
    var blockOwnerId: Long = 0,
    var name: String = "",
    var reps: Int = 0,
    var unit: String = "reps",
    var weightKg: Int? = null
)