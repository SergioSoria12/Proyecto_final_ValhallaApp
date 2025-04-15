package edu.sergiosoria.valhallathebox.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import edu.sergiosoria.valhallathebox.R
import edu.sergiosoria.valhallathebox.database.AppDatabase
import edu.sergiosoria.valhallathebox.models.ExerciseLine
import edu.sergiosoria.valhallathebox.models.Wod
import edu.sergiosoria.valhallathebox.models.WodBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import edu.sergiosoria.valhallathebox.ValhallaApp

class CreateWodActivity : AppCompatActivity() {

    private lateinit var exerciseList: LinearLayout
    private lateinit var db: AppDatabase
    private val addedExercises = mutableListOf<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_wod)

        db = ValhallaApp.database

        exerciseList = findViewById(R.id.exerciseList)

        findViewById<Button>(R.id.btnAddExercise).setOnClickListener {
            addExerciseInput()
        }

        findViewById<Button>(R.id.btnSaveWod).setOnClickListener {
            saveWod()
        }
    }

    private fun addExerciseInput() {
        val inflater = LayoutInflater.from(this)
        val exerciseView = inflater.inflate(R.layout.item_exercise_input, exerciseList, false)
        exerciseList.addView(exerciseView)
        addedExercises.add(exerciseView)
    }

    private fun saveWod() {
        val level = when (findViewById<RadioGroup>(R.id.radioLevel).checkedRadioButtonId) {
            R.id.rbScaled -> "scaled"
            R.id.rbRX -> "rx"
            R.id.rbElite -> "elite"
            else -> "scaled"
        }

        lifecycleScope.launch {
            // 1. Guardar WOD en Room
            val wod = Wod(name = "WOD creado", type = "EMOM", imageUri = null, rounds = 5, roundTime = 2)
            val wodId = db.wodDao().insertWod(wod)
            val insertedWod = wod.copy(wodId = wodId)

            val block = WodBlock(wodOwnerId = wodId, level = level)
            val blockId = db.wodDao().insertBlock(block)
            val insertedBlock = block.copy(blockId = blockId)

            val exercises = mutableListOf<ExerciseLine>()
            for (view in addedExercises) {
                val name = view.findViewById<EditText>(R.id.inputName).text.toString()
                val reps = view.findViewById<EditText>(R.id.inputReps).text.toString().toIntOrNull() ?: 0
                val weight = view.findViewById<EditText>(R.id.inputWeight).text.toString().toIntOrNull()

                val ex = ExerciseLine(blockOwnerId = blockId, name = name, reps = reps, weightKg = weight)
                db.wodDao().insertExercise(ex)
                exercises.add(ex)
            }

            // 2. SUBIR A FIREBASE
            val dbRef = Firebase.database.reference.child("wods").child(wodId.toString())

            val wodMap = mapOf(
                "name" to insertedWod.name,
                "type" to insertedWod.type,
                "imageUri" to insertedWod.imageUri,
                "rounds" to insertedWod.rounds,
                "roundTime" to insertedWod.roundTime
            )

            val blockMap = mapOf(
                "level" to insertedBlock.level,
                "exercises" to exercises.map {
                    mapOf(
                        "name" to it.name,
                        "reps" to it.reps,
                        "weightKg" to it.weightKg
                    )
                }
            )

            dbRef.setValue(mapOf(
                "info" to wodMap,
                "block" to blockMap
            ))

            // 3. Finalizar
            withContext(Dispatchers.Main) {
                Toast.makeText(this@CreateWodActivity, "WOD guardado", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}