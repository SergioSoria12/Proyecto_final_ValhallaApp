package edu.sergiosoria.valhallathebox.utils

import androidx.room.Embedded
import androidx.room.Relation

import edu.sergiosoria.valhallathebox.models.*


data class WodWithBlocks(
    @Embedded var wod: Wod = Wod(),

    @Relation(
        parentColumn = "wodId",
        entityColumn = "wodOwnerId",
        entity = WodBlock::class

    )
    var blocks: List<BlockWithExercises> = emptyList()
)

data class BlockWithExercises(
    @Embedded var block: WodBlock = WodBlock(),

    @Relation(
        parentColumn = "blockId",
        entityColumn = "blockOwnerId"
    )
    var exercises: List<ExerciseLine> = emptyList()
)