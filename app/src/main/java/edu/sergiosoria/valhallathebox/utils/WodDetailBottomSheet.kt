package edu.sergiosoria.valhallathebox.utils

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import edu.sergiosoria.valhallathebox.R
import edu.sergiosoria.valhallathebox.activities.CreateWodActivity
import edu.sergiosoria.valhallathebox.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WodDetailBottomSheet(
    private val wodWithBlocks: WodWithBlocks
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wod_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvWodName = view.findViewById<TextView>(R.id.tvWodName)
        val blocksContainer = view.findViewById<LinearLayout>(R.id.blocksContainer)
        val tvRounds = view.findViewById<TextView>(R.id.tvRounds)
        val tvTime = view.findViewById<TextView>(R.id.tvTime)

        val wod = wodWithBlocks.wod
        tvWodName.text = wod.type
        tvRounds.text = "${wod.rounds} RONDAS"
        tvTime.text = " - ${wod.roundTime} MIN ROUND"

        for (block in wodWithBlocks.blocks) {
            val blockTitle = TextView(requireContext()).apply {
                text = block.block.level.uppercase()
                setTextColor(Color.DKGRAY)
                setPadding(0, 12, 0, 4)
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
            }
            blocksContainer.addView(blockTitle)

            for (ex in block.exercises) {
                val exerciseView = layoutInflater.inflate(R.layout.item_exercise_view, null)

                exerciseView.findViewById<TextView>(R.id.tvExerciseName).text = ex.name
                exerciseView.findViewById<TextView>(R.id.tvExerciseReps).text = "${ex.reps} reps"
                exerciseView.findViewById<TextView>(R.id.tvExerciseWeight).text =
                    if (ex.weightKg != null) "${ex.weightKg} kg" else ""

                blocksContainer.addView(exerciseView)
            }
        }

        //Logica para los botones de editar e eliminar
        val btnEdit = view.findViewById<Button>(R.id.btnEditWod)
        val btnDelete = view.findViewById<Button>(R.id.btnDeleteWod)

        btnEdit.setOnClickListener {
            val intent = Intent(requireContext(), CreateWodActivity::class.java)
            intent.putExtra("EDIT_WOD_ID", wodWithBlocks.wod.wodId)
            startActivity(intent)
            dismiss()
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("¿Eliminar WOD?")
                .setMessage("¿Seguro que deseas eliminar este WOD?")
                .setPositiveButton("Sí") { _, _ ->
                    lifecycleScope.launch {
                        val db = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "app-db").build()
                        db.wodDao().deleteWod(wodWithBlocks.wod)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "WOD eliminado", Toast.LENGTH_SHORT).show()
                            dismiss()
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}