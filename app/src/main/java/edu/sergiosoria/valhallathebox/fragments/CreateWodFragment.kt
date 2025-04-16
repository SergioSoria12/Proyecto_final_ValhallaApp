package edu.sergiosoria.valhallathebox.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.FirebaseDatabase
import edu.sergiosoria.valhallathebox.R
import edu.sergiosoria.valhallathebox.database.AppDatabase
import edu.sergiosoria.valhallathebox.models.ExerciseLine
import edu.sergiosoria.valhallathebox.models.Wod
import edu.sergiosoria.valhallathebox.models.WodBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import edu.sergiosoria.valhallathebox.ValhallaApp
import edu.sergiosoria.valhallathebox.utils.WodWithBlocks
import kotlinx.coroutines.flow.first

class CreateWodFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var exerciseList: LinearLayout
    private val addedExercises = mutableListOf<View>()
    private var editWodId: Long? = null
    private var isEditing = false
    private var selectedImageUri: String? = null
    private lateinit var imagePreview: ImageView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_wod, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        db = ValhallaApp.database
        exerciseList = view.findViewById(R.id.exerciseList)

        //Inicializamos el imageView
        imagePreview = view.findViewById(R.id.wodImagePreview)

        view.findViewById<Button>(R.id.btnSelectImage).setOnClickListener {
            showImagePickerDialog()
        }

        val radioType = view.findViewById<RadioGroup>(R.id.radioType)
        val layoutEmom = view.findViewById<LinearLayout>(R.id.layoutEmomData)
        val inputAmrap = view.findViewById<EditText>(R.id.inputAmrapTime)

        radioType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbEmom) {
                layoutEmom.visibility = View.VISIBLE
                inputAmrap.visibility = View.GONE
            } else {
                layoutEmom.visibility = View.GONE
                inputAmrap.visibility = View.VISIBLE
            }
        }

        view.findViewById<Button>(R.id.btnAddExercise).setOnClickListener {
            addExerciseInput()
        }

        view.findViewById<Button>(R.id.btnSaveWod).setOnClickListener {
            saveWod(view)
        }

        // Si venimos a editar
        editWodId = arguments?.getLong("EDIT_WOD_ID")
        if (editWodId != null) {
            isEditing = true
            lifecycleScope.launch(Dispatchers.IO) {
                val wodWithBlocks = db.wodDao().getWodByIdFlow(editWodId!!).first()
                withContext(Dispatchers.Main) {
                    if (wodWithBlocks != null) {
                        loadWodDataIntoForm(wodWithBlocks)
                    }
                }
            }
        }
    }

    private fun addExerciseInput(
        name: String = "",
        reps: Int = 0,
        weight: Int? = null,
        unit: String = "reps"

    ) {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.item_exercise_input, exerciseList, false)
        view.setBackgroundResource(R.drawable.rounded_background_grey)
        view.setPadding(16, 16, 16, 16)

        //Autocomplete para nombres
        val ejercicios = listOf("Burpees", "Hand Clean", "Push Jerk", "Thruster", "Row", "Bike")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, ejercicios)
        view.findViewById<AutoCompleteTextView>(R.id.inputName).setAdapter(adapter)
        view.findViewById<AutoCompleteTextView>(R.id.inputName).setText(name)

        // Reps
        view.findViewById<EditText>(R.id.inputReps).setText(if (reps > 0) reps.toString() else "")

        // Spinner unidad Aqui asignamos (reps, cal, m)
        val spinnerUnidad = view.findViewById<Spinner>(R.id.spinnerUnidad)
        val unidades = resources.getStringArray(R.array.unidades)
        val indexUnidad = unidades.indexOf(unit)
        if (indexUnidad >= 0) spinnerUnidad.setSelection(indexUnidad)

        //Peso
        view.findViewById<EditText>(R.id.inputWeight).setText(weight?.toString() ?: "")

        exerciseList.addView(view)
        addedExercises.add(view)
    }

    private fun loadWodDataIntoForm(wodWithBlocks: WodWithBlocks) {
        val view = requireView()

        val wod = wodWithBlocks.wod
        val block = wodWithBlocks.blocks.firstOrNull()
        val exercises = block?.exercises ?: emptyList()

        // Tipo
        when (wod.type) {
            "EMOM" -> view.findViewById<RadioButton>(R.id.rbEmom).isChecked = true
            "AMRAP" -> view.findViewById<RadioButton>(R.id.rbAmrap).isChecked = true
        }

        // Nivel
        when (block?.block?.level) {
            "scaled" -> view.findViewById<RadioButton>(R.id.rbScaled).isChecked = true
            "rx" -> view.findViewById<RadioButton>(R.id.rbRX).isChecked = true
            "elite" -> view.findViewById<RadioButton>(R.id.rbElite).isChecked = true
        }

        // Rondas / tiempo
        if (wod.type == "EMOM") {
            view.findViewById<EditText>(R.id.inputRounds).setText(wod.rounds.toString())
            view.findViewById<EditText>(R.id.inputRoundTime).setText(wod.roundTime.toString())
        } else {
            view.findViewById<EditText>(R.id.inputAmrapTime).setText(wod.roundTime.toString())
        }

        // Ejercicios
        exercises.forEach {
            addExerciseInput(it.name, it.reps, it.weightKg, it.unit)
        }
    }

    private fun saveWod(view: View) {
        val type = when (view.findViewById<RadioGroup>(R.id.radioType).checkedRadioButtonId) {
            R.id.rbEmom -> "EMOM"
            R.id.rbAmrap -> "AMRAP"
            else -> "EMOM"
        }

        val level = when (view.findViewById<RadioGroup>(R.id.radioLevel).checkedRadioButtonId) {
            R.id.rbScaled -> "scaled"
            R.id.rbRX -> "rx"
            R.id.rbElite -> "elite"
            else -> "scaled"
        }

        val rounds = if (type == "EMOM") {
            view.findViewById<EditText>(R.id.inputRounds).text.toString().toIntOrNull() ?: 0
        } else 1

        val roundTime = if (type == "EMOM") {
            view.findViewById<EditText>(R.id.inputRoundTime).text.toString().toIntOrNull() ?: 0
        } else {
            view.findViewById<EditText>(R.id.inputAmrapTime).text.toString().toIntOrNull() ?: 0
        }

        val wodName = view.findViewById<EditText>(R.id.inputWodName).text.toString()

        lifecycleScope.launch(Dispatchers.IO) {
            val wodId = if (isEditing && editWodId != null) {
                // Eliminamos el anterior
                db.wodDao().deleteWod(Wod(wodId = editWodId!!))
                editWodId!!
            } else {
                db.wodDao().insertWod(Wod(name = wodName, type = type, rounds = rounds, roundTime = roundTime))
            }

            val blockId = db.wodDao().insertBlock(WodBlock(wodOwnerId = wodId, level = level))

            for (v in addedExercises) {
                val name = v.findViewById<EditText>(R.id.inputName).text.toString()
                val reps = v.findViewById<EditText>(R.id.inputReps).text.toString().toIntOrNull() ?: 0
                val weight = v.findViewById<EditText>(R.id.inputWeight).text.toString().toIntOrNull()
                val unit = v.findViewById<Spinner>(R.id.spinnerUnidad).selectedItem.toString()

                db.wodDao().insertExercise(
                    ExerciseLine(blockOwnerId = blockId, name = name, reps = reps, unit = unit, weightKg = weight)
                )
            }

            // 🔥 SUBIR A FIREBASE
            val firebaseDb = FirebaseDatabase.getInstance("https://valhallathebox-default-rtdb.europe-west1.firebasedatabase.app/")
            val wodRef = firebaseDb.getReference("wods")
            wodRef.child(wodId.toString()).setValue(Wod(wodId = wodId, name = "WOD creado", type = type, rounds = rounds, roundTime = roundTime))

            // Guardar la imagen en Room
            db.wodDao().updateImage(wodId, selectedImageUri)
            // Volvemos al hilo principal
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "WOD guardado", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun showImagePickerDialog() {
        val images = listOf(
            R.drawable.wod1,
            R.drawable.wod2,
        )
        val imageNames = listOf("WOD 1", "WOD 2", "WOD 3", "WOD 4")

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Selecciona una imagen")

        builder.setItems(imageNames.toTypedArray()) { _, which ->
            val resId = images[which]
            selectedImageUri = "android.resource://${requireContext().packageName}/$resId"
            imagePreview.setImageURI(Uri.parse(selectedImageUri))
            imagePreview.visibility = View.VISIBLE
        }

        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }
}