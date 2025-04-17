package edu.sergiosoria.valhallathebox.database

import androidx.room.*
import edu.sergiosoria.valhallathebox.models.ExerciseLine
import edu.sergiosoria.valhallathebox.models.Wod
import edu.sergiosoria.valhallathebox.models.WodBlock
import edu.sergiosoria.valhallathebox.utils.WodWithBlocks
import kotlinx.coroutines.flow.Flow


@Dao
interface WodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWod(wod: Wod): Long
    @Insert fun insertBlock(block: WodBlock): Long
    @Insert fun insertExercise(exercise: ExerciseLine): Long

    @Delete
    fun deleteWod(wod: Wod)

    @Query("UPDATE Wod SET imageUri = :imageRes WHERE wodId = :id")
    fun updateImage(id: Long, imageRes: String?)

    @Update
    fun updateWod(wod: Wod)

    @Query("DELETE FROM WodBlock WHERE wodOwnerId = :wodId")
    fun deleteBlocksByWodId(wodId: Long)

    @Query("DELETE FROM ExerciseLine WHERE blockOwnerId = :blockId")
    fun deleteExercisesByBlockId(blockId: Long)

    @Query("SELECT * FROM Wod WHERE isFavorite = 1")
    fun getFavoriteWods(): Flow<List<WodWithBlocks>>

    @Transaction
    @Query("SELECT * FROM Wod")
    fun getAllWods(): Flow<List<WodWithBlocks>>

    @Transaction
    @Query("SELECT * FROM Wod WHERE wodId = :id")
    fun getWodByIdFlow(id: Long): Flow<WodWithBlocks?>


}