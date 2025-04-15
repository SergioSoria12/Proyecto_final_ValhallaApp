package edu.sergiosoria.valhallathebox

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.google.firebase.FirebaseApp
import edu.sergiosoria.valhallathebox.database.AppDatabase

class ValhallaApp : Application() {
    companion object {
        lateinit var instance: ValhallaApp
        lateinit var database: AppDatabase
        lateinit var preferences: SharedPreferences
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        instance = this
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "valhalla_db"
        ).fallbackToDestructiveMigration().build()
        preferences = getSharedPreferences("valhalla_prefs", Context.MODE_PRIVATE)
    }
}
