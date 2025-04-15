package edu.sergiosoria.valhallathebox.database

import androidx.room.*
import edu.sergiosoria.valhallathebox.models.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    fun getUser(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users")
    fun getAllUsers(): List<User>

    @Delete
    fun deleteUser(user: User)
}