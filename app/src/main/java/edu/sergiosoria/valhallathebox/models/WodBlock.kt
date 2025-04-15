package edu.sergiosoria.valhallathebox.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Wod::class,
        parentColumns = ["wodId"],
        childColumns = ["wodOwnerId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class WodBlock(
    @PrimaryKey(autoGenerate = true) var blockId: Long = 0,
    var wodOwnerId: Long = 0,
    var level: String = "", // scaled, RX, elite
)