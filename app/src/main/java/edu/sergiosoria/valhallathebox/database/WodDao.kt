package edu.sergiosoria.valhallathebox.database

import androidx.room.*
import edu.sergiosoria.valhallathebox.models.ExerciseLine
import edu.sergiosoria.valhallathebox.models.Wod
import edu.sergiosoria.valhallathebox.models.WodBlock
import edu.sergiosoria.valhallathebox.utils.WodWithBlocks
import kotlinx.coroutines.flow.Flow


@Dao
interface WodDao {
    @Insert fun insertWod(wod: Wod): Long
    @Insert fun insertBlock(block: WodBlock): Long
    @Insert fun insertExercise(exercise: ExerciseLine): Long

    @Delete
    fun deleteWod(wod: Wod)

    @Transaction
    @Query("SELECT * FROM Wod")
    fun getAllWods(): Flow<List<WodWithBlocks>>

    @Transaction
    @Query("SELECT * FROM Wod WHERE wodId = :id")
    fun getWodByIdFlow(id: Long): Flow<WodWithBlocks?>


}