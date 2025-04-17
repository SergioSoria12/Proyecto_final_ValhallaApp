package edu.sergiosoria.valhallathebox.utils

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.FirebaseDatabase
import edu.sergiosoria.valhallathebox.R
import edu.sergiosoria.valhallathebox.ValhallaApp
import edu.sergiosoria.valhallathebox.fragments.CreateWodFragment
import edu.sergiosoria.valhallathebox.database.AppDatabase
import edu.sergiosoria.valhallathebox.fragments.WodListFragment
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
        val tvWodType = view.findViewById<TextView>(R.id.tvWodType)
        val tvWodName = view.findViewById<TextView>(R.id.tvWodName)
        val blocksContainer = view.findViewById<LinearLayout>(R.id.blocksContainer)
        val tvRounds = view.findViewById<TextView>(R.id.tvRounds)
        val tvTime = view.findViewById<TextView>(R.id.tvTime)

        val wod = wodWithBlocks.wod


        tvWodType.text = wod.type
        tvWodName.text = wod.name
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
                exerciseView.findViewById<TextView>(R.id.tvExerciseReps).text = "${ex.reps} ${ex.unit}"
                exerciseView.findViewById<TextView>(R.id.tvExerciseWeight).text =
                    if (ex.weightKg != null) "${ex.weightKg} kg" else ""

                blocksContainer.addView(exerciseView)
            }
        }

        //Logica para los botones de editar e eliminar
        val btnEdit = view.findViewById<Button>(R.id.btnEditWod)
        val btnDelete = view.findViewById<Button>(R.id.btnDeleteWod)

        btnEdit.setOnClickListener {
            val fragment = CreateWodFragment().apply {
                arguments = Bundle().apply {
                    putLong("EDIT_WOD_ID", wodWithBlocks.wod.wodId)
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()

            dismiss()
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("¿Eliminar WOD?")
                .setMessage("¿Seguro que deseas eliminar este WOD?")
                .setPositiveButton("Sí") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        val db = ValhallaApp.database
                        db.wodDao().deleteWod(wodWithBlocks.wod)

                        // 🔥 ELIMINAR EN FIREBASE
                        val firebaseDb = FirebaseDatabase.getInstance("https://valhallathebox-default-rtdb.europe-west1.firebasedatabase.app/")
                        val wodRef = firebaseDb.getReference("wods").child(wodWithBlocks.wod.wodId.toString())
                        wodRef.removeValue()

                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "WOD eliminado", Toast.LENGTH_SHORT).show()
                            dismiss()

                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, WodListFragment())
                                .commit()
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}