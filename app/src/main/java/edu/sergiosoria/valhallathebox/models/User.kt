package edu.sergiosoria.valhallathebox.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String = "",
    var surname: String = "",
    var telephone: String = "",
    var city: String = "",
    var email: String = "",
    var password: String = "",
    var type: Boolean = false, // true = Atleta, false = Coach
    var avatar: String = "" //Guardamos la URI de  la imagen en formato String
)