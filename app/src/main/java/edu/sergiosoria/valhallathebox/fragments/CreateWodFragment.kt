package edu.sergiosoria.valhallathebox.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.flow.firstOrNull
import edu.sergiosoria.valhallathebox.utils.getWodImageRes


class CreateWodFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var exerciseList: LinearLayout
    private val addedExercises = mutableListOf<View>()
    private var editWodId: Long? = null
    private var isEditing = false
    private var selectedImageName: String? = null
    private lateinit var imagePreview: ImageView
    private var loadedBlockId: Long? = null
    private var loadedExercises: List<ExerciseLine> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_create_wod, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        db = ValhallaApp.database
        exerciseList = view.findViewById(R.id.exerciseList)
        imagePreview = view.findViewById(R.id.wodImagePreview)

        view.findViewById<Button>(R.id.btnSelectImage).setOnClickListener {
            showImagePickerDialog()
        }

        // Mostrar secciones segun el tipo (EMOM o AMRAP)
        val radioType = view.findViewById<RadioGroup>(R.id.radioType)
        val layoutEmom = view.findViewById<LinearLayout>(R.id.layoutEmomData)
        val inputAmrap = view.findViewById<EditText>(R.id.inputAmrapTime)
        radioType.setOnCheckedChangeListener { _, checkedId ->
            layoutEmom.visibility = if (checkedId == R.id.rbEmom) View.VISIBLE else View.GONE
            inputAmrap.visibility = if (checkedId == R.id.rbAmrap) View.VISIBLE else View.GONE
        }

        view.findViewById<Button>(R.id.btnAddExercise).setOnClickListener {
            addExerciseInput()
        }

        view.findViewById<Button>(R.id.btnSaveWod).setOnClickListener {
            saveWod(view)
        }

        // Cargar datos si estamos editando
        editWodId = arguments?.getLong("EDIT_WOD_ID")
        if (editWodId != null) {
            isEditing = true
            lifecycleScope.launch(Dispatchers.IO) {
                val wodWithBlocks = db.wodDao().getWodByIdFlow(editWodId!!).firstOrNull()
                wodWithBlocks?.let {
                    loadedBlockId = it.blocks.firstOrNull()?.block?.blockId
                    loadedExercises = it.blocks.firstOrNull()?.exercises ?: emptyList()
                    withContext(Dispatchers.Main) {
                        loadWodDataIntoForm(it)
                    }
                }
            }
        }
    }

    private fun addExerciseInput(name: String = "", reps: Int = 0, weight: Int? = null, unit: String = "reps") {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.item_exercise_input, exerciseList, false)
        view.setBackgroundResource(R.drawable.rounded_button_grey)
        view.setPadding(16, 16, 16, 16)

        val ejercicios = resources.getStringArray(R.array.ejercicios_crossfit)
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, ejercicios)

        val inputName = view.findViewById<AutoCompleteTextView>(R.id.inputName)
        inputName.setAdapter(adapter)
        inputName.setText(name)

        // 👇 Esto hace que el dropdown sea tan ancho como la mitad pantalla
        val dropdownWidth = (resources.displayMetrics.widthPixels * 0.5).toInt()
        inputName.setDropDownWidth(dropdownWidth)

        view.findViewById<EditText>(R.id.inputReps).setText(if (reps > 0) reps.toString() else "")
        view.findViewById<EditText>(R.id.inputWeight).setText(weight?.toString() ?: "")

        val spinnerUnidad = view.findViewById<Spinner>(R.id.spinnerUnidad)
        val unidades = resources.getStringArray(R.array.unidades)
        spinnerUnidad.setSelection(unidades.indexOf(unit))

        exerciseList.addView(view)
        addedExercises.add(view)
    }

    private fun loadWodDataIntoForm(wodWithBlocks: WodWithBlocks) {
        val view = requireView()
        val wod = wodWithBlocks.wod

        view.findViewById<EditText>(R.id.inputWodName).setText(wod.name)

        selectedImageName = wod.imageUri
        selectedImageName?.let {
            val resId = getWodImageRes(it)
            imagePreview.setImageResource(resId)
            imagePreview.visibility = View.VISIBLE
        }

        // Selección de tipo y nivel
        when (wod.type) {
            "EMOM" -> view.findViewById<RadioButton>(R.id.rbEmom).isChecked = true
            "AMRAP" -> view.findViewById<RadioButton>(R.id.rbAmrap).isChecked = true
        }

        when (wodWithBlocks.blocks.firstOrNull()?.block?.level) {
            "scaled" -> view.findViewById<RadioButton>(R.id.rbScaled).isChecked = true
            "rx" -> view.findViewById<RadioButton>(R.id.rbRX).isChecked = true
            "elite" -> view.findViewById<RadioButton>(R.id.rbElite).isChecked = true
        }

        if (wod.type == "EMOM") {
            view.findViewById<EditText>(R.id.inputRounds).setText(wod.rounds.toString())
            view.findViewById<EditText>(R.id.inputRoundTime).setText(wod.roundTime.toString())
        } else {
            view.findViewById<EditText>(R.id.inputAmrapTime).setText(wod.roundTime.toString())
        }

        wodWithBlocks.blocks.firstOrNull()?.exercises?.forEach {
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
        val rounds = if (type == "EMOM") view.findViewById<EditText>(R.id.inputRounds).text.toString().toIntOrNull() ?: 0 else 1
        val roundTime = if (type == "EMOM")
            view.findViewById<EditText>(R.id.inputRoundTime).text.toString().toIntOrNull() ?: 0
        else
            view.findViewById<EditText>(R.id.inputAmrapTime).text.toString().toIntOrNull() ?: 0
        val wodName = view.findViewById<EditText>(R.id.inputWodName).text.toString()

        lifecycleScope.launch(Dispatchers.IO) {
            val wod = Wod(
                wodId = editWodId ?: 0L,
                name = wodName,
                type = type,
                rounds = rounds,
                roundTime = roundTime,
                imageUri = selectedImageName
            )
            val wodId = db.wodDao().insertWod(wod)

            if (isEditing) db.wodDao().deleteBlocksByWodId(wodId)

            val blockObj = WodBlock(blockId = loadedBlockId ?: 0L, wodOwnerId = wodId, level = level)
            val blockId = db.wodDao().insertBlock(blockObj)

            db.wodDao().deleteExercisesByBlockId(blockId)

            for (i in addedExercises.indices) {
                val v = addedExercises[i]
                val name = v.findViewById<EditText>(R.id.inputName).text.toString()
                val reps = v.findViewById<EditText>(R.id.inputReps).text.toString().toIntOrNull() ?: 0
                val weight = v.findViewById<EditText>(R.id.inputWeight).text.toString().toIntOrNull()
                val unit = v.findViewById<Spinner>(R.id.spinnerUnidad).selectedItem.toString()

                val previousId = loadedExercises.getOrNull(i)?.exerciseId ?: 0L
                val exercise = ExerciseLine(previousId, blockId, name, reps, unit, weight)
                db.wodDao().insertExercise(exercise)
            }

            db.wodDao().updateImage(wodId, selectedImageName)

            // Subida a Firebase
            val firebaseDb = FirebaseDatabase.getInstance("https://valhallathebox-default-rtdb.europe-west1.firebasedatabase.app/")
            val wodRef = firebaseDb.getReference("wods")
            if (isEditing && editWodId != null) {
                wodRef.child(editWodId.toString()).removeValue()
            }
            wodRef.child(wodId.toString()).setValue(wod)
            val blocksRef = wodRef.child("$wodId/blocks")
            blocksRef.child(blockId.toString()).setValue(blockObj)
            val exRef = blocksRef.child("$blockId/exercises")
            for (i in addedExercises.indices) {
                val v = addedExercises[i]
                val name = v.findViewById<EditText>(R.id.inputName).text.toString()
                val reps = v.findViewById<EditText>(R.id.inputReps).text.toString().toIntOrNull() ?: 0
                val weight = v.findViewById<EditText>(R.id.inputWeight).text.toString().toIntOrNull()
                val unit = v.findViewById<Spinner>(R.id.spinnerUnidad).selectedItem.toString()
                val exercise = ExerciseLine(0L, blockId, name, reps, unit, weight)
                exRef.push().setValue(exercise)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "WOD guardado correctamente", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun showImagePickerDialog() {
        val wodNames = (1..12).map { "WOD $it" }

        val gridLayout = GridLayout(requireContext()).apply {
            columnCount = 3
            setPadding(32, 32, 32, 32)
            alignmentMode = GridLayout.ALIGN_MARGINS
            useDefaultMargins = true
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
        }

        val scrollView = ScrollView(requireContext()).apply {
            addView(gridLayout)
            setPadding(16, 16, 16, 16)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Selecciona una imagen")
            .setView(scrollView)
            .setNegativeButton("Cancelar", null)
            .create()

        wodNames.forEach { name ->
            val resId = getWodImageRes(name)

            val container = FrameLayout(requireContext()).apply {
                layoutParams = ViewGroup.MarginLayoutParams(250, 250).apply {
                    setMargins(16, 16, 16, 16)
                }
                background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_border_wod_image)
            }

            val imageView = ImageView(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                setImageResource(resId)
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = name
                setOnClickListener {
                    selectedImageName = name
                    imagePreview.setImageResource(resId)
                    imagePreview.visibility = View.VISIBLE
                    dialog.dismiss()
                }
            }

            container.addView(imageView)
            gridLayout.addView(container)
        }

        dialog.show()
    }
}