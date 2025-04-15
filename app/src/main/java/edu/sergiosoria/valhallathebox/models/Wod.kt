package edu.sergiosoria.valhallathebox.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Wod(
    @PrimaryKey(autoGenerate = true) var wodId: Long = 0,
    var name: String = "",
    var type: String = "", // "EMOM", "AMRAP", etc.
    var imageUri: String? = null, // URI de la imagen
    var rounds: Int = 0,
    var roundTime: Int = 0 // en minutos
)