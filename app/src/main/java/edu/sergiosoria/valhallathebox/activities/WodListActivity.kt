package edu.sergiosoria.valhallathebox.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.sergiosoria.valhallathebox.R
import edu.sergiosoria.valhallathebox.adapters.WodAdapter
import edu.sergiosoria.valhallathebox.database.AppDatabase
import edu.sergiosoria.valhallathebox.models.Wod
import edu.sergiosoria.valhallathebox.utils.WodDetailBottomSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WodListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var wodAdapter: WodAdapter
    private lateinit var db: AppDatabase
    private val wodList = mutableListOf<Wod>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wod_list)

        db = AppDatabase.getDatabase(applicationContext)

        recyclerView = findViewById(R.id.wodRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        wodAdapter = WodAdapter(wodList) { wod ->
            lifecycleScope.launch {
                val wodWithBlocks = db.wodDao().getWodByIdFlow(wod.wodId).first()
                wodWithBlocks?.let { WodDetailBottomSheet(it).show(supportFragmentManager, "WodDetail") }
            }
        }
        recyclerView.adapter = wodAdapter

        findViewById<FloatingActionButton>(R.id.fabAddWod).setOnClickListener {
            val intent = Intent(this, CreateWodActivity::class.java)
            startActivity(intent)
        }

        loadWods()
    }

    private fun loadWods() {
        lifecycleScope.launch {
            db.wodDao().getAllWods().collect { fullWods ->
                wodList.clear()
                wodList.addAll(fullWods.map { it.wod })
                withContext(Dispatchers.Main) {
                    wodAdapter.notifyDataSetChanged()
                }
            }
        }
    }
}