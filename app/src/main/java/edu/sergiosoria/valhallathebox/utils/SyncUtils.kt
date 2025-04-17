package edu.sergiosoria.valhallathebox.utils

import android.content.Context
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import edu.sergiosoria.valhallathebox.ValhallaApp
import edu.sergiosoria.valhallathebox.models.ExerciseLine
import edu.sergiosoria.valhallathebox.models.User
import edu.sergiosoria.valhallathebox.models.Wod
import edu.sergiosoria.valhallathebox.models.WodBlock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object SyncUtils {

    //Funcion para cargar bases de datos al arrancar
    suspend fun syncAllToFirebase() = withContext(Dispatchers.IO) {
        val db = ValhallaApp.database
        val firebaseDB = FirebaseDatabase.getInstance("https://valhallathebox-default-rtdb.europe-west1.firebasedatabase.app/")

        // Sync Users
        val users = db.userDao().getAllUsers()
        val usersRef = firebaseDB.getReference("users")
        users.forEach {
            usersRef.child(it.id.toString()).setValue(it)
        }

        // Sync WODs
        val wodWithBlocks = db.wodDao().getAllWods().first()
        val wodsRef = firebaseDB.getReference("wods")
        for (wodFull in wodWithBlocks) {
            val wod = wodFull.wod
            wodsRef.child(wod.wodId.toString()).setValue(wod)

            val blocksRef = wodsRef.child("${wod.wodId}/blocks")
            wodFull.blocks.forEach { blockWithExercises ->
                val blockId = blockWithExercises.block.blockId.toString()
                blocksRef.child(blockId).setValue(blockWithExercises.block)

                val exRef = blocksRef.child("$blockId/exercises")
                blockWithExercises.exercises.forEach { ex ->
                    exRef.child(ex.exerciseId.toString()).setValue(ex)
                }
            }
        }

        Log.d("SYNC", "✅ Sincronización completada con Firebase")
    }

    fun syncFirebaseToRoom(context: Context) {
        val db = ValhallaApp.database
        val firebaseDb = FirebaseDatabase.getInstance("https://valhallathebox-default-rtdb.europe-west1.firebasedatabase.app/")

        CoroutineScope(Dispatchers.IO).launch {
            // 🔁 Sincronizar usuarios
            firebaseDb.getReference("users").get().addOnSuccessListener { snapshot ->
                snapshot.children.forEach { userSnapshot ->
                    val user = userSnapshot.getValue(User::class.java)
                    user?.let {
                        CoroutineScope(Dispatchers.IO).launch {
                            db.userDao().insertUser(it)
                        }
                    }
                }
            }

            // 🔁 Sincronizar WODs con bloques y ejercicios
            firebaseDb.getReference("wods").get().addOnSuccessListener { snapshot ->
                snapshot.children.forEach { wodSnapshot ->
                    val wod = wodSnapshot.getValue(Wod::class.java)
                    wod?.let { wodData ->
                        CoroutineScope(Dispatchers.IO).launch {
                            val wodId = db.wodDao().insertWod(wodData)

                            // Bloques
                            wodSnapshot.child("blocks").children.forEach { blockSnap ->
                                val block = blockSnap.getValue(WodBlock::class.java)
                                block?.let {
                                    it.wodOwnerId = wodId
                                    val blockId = db.wodDao().insertBlock(it)

                                    // Ejercicios
                                    blockSnap.child("exercises").children.forEach { exSnap ->
                                        val exercise = exSnap.getValue(ExerciseLine::class.java)
                                        exercise?.let { ex ->
                                            ex.blockOwnerId = blockId
                                            db.wodDao().insertExercise(ex)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}