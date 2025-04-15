package edu.sergiosoria.valhallathebox.database

import androidx.room.*
import edu.sergiosoria.valhallathebox.models.ClassSession

@Dao
interface ClassSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSession(session: ClassSession)

    @Query("SELECT * FROM class_sessions WHERE assignedTo = :email ORDER BY dateTime ASC")
    fun getUpcomingSessions(email: String ): List<ClassSession>
}