package edu.sergiosoria.valhallathebox.fragments

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.sergiosoria.valhallathebox.R
import edu.sergiosoria.valhallathebox.ValhallaApp
import edu.sergiosoria.valhallathebox.adapters.WodAdapter
import edu.sergiosoria.valhallathebox.database.AppDatabase
import edu.sergiosoria.valhallathebox.models.Wod
import edu.sergiosoria.valhallathebox.utils.WodDetailBottomSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WodListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var wodAdapter: WodAdapter
    private lateinit var db: AppDatabase
    private val wodList = mutableListOf<Wod>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wod_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("WOD_FRAGMENT", "Fragment cargado correctamente")
        db = ValhallaApp.database

        recyclerView = view.findViewById(R.id.wodRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // 👉 Añadir separación entre ítems
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
            ) {
                outRect.set(16, 16, 16, 16) // Margen en cada lado de la tarjeta
            }
        })

        wodAdapter = WodAdapter(
            wodList,
            onWodClick = { wod ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val wodWithBlocks = db.wodDao().getWodByIdFlow(wod.wodId).first()
                    wodWithBlocks?.let {
                        WodDetailBottomSheet(it).show(parentFragmentManager, "WodDetail")
                    }
                }
            },
            onFavoriteClick = { wod ->
                val updatedWod = wod.copy(isFavorite = !wod.isFavorite)
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    db.wodDao().updateWod(updatedWod)
                }
            }
        )
        recyclerView.adapter = wodAdapter

        val fab = view.findViewById<FloatingActionButton>(R.id.fabAddWod)

        fab.setOnClickListener {
            // 👇 Animación bounce
            val bounce = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
            fab.startAnimation(bounce)

            // 👇 Abrir el fragmento para crear el WOD
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateWodFragment())
                .addToBackStack(null)
                .commit()
        }

        loadWods()
    }

    private fun loadWods() {
        viewLifecycleOwner.lifecycleScope.launch {
            db.wodDao().getAllWods().collect { fullWods ->
                wodList.clear()
                wodList.addAll(fullWods.map { it.wod })
                Log.d("WOD_FRAGMENT", "Cargando WODs desde Room...")
                withContext(Dispatchers.Main) {
                    wodAdapter.notifyDataSetChanged()
                }
            }
        }
    }
}
