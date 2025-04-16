package edu.sergiosoria.valhallathebox.adapters

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import edu.sergiosoria.valhallathebox.R
import edu.sergiosoria.valhallathebox.models.Wod

class WodAdapter(
    private val wodList: List<Wod>,
    private val onWodClick: (Wod) -> Unit
) : RecyclerView.Adapter<WodAdapter.WodViewHolder>() {

    inner class WodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imageWod)
        val title: TextView = view.findViewById(R.id.titleWod)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wod, parent, false)
        return WodViewHolder(view)
    }

    override fun onBindViewHolder(holder: WodViewHolder, position: Int) {
        val wod = wodList[position]
        holder.title.text = wod.name


        if (!wod.imageUri.isNullOrEmpty()) {
            holder.image.setImageURI(Uri.parse(wod.imageUri))
        } else {
            holder.image.setImageResource(R.drawable.wod2) // por si no hay imagen
        }

        // Animación al cargar
        holder.itemView.alpha = 0f
        holder.itemView.animate().alpha(1f).setDuration(500).start()

        holder.itemView.setOnClickListener {
            onWodClick(wod)
        }
    }

    override fun getItemCount(): Int = wodList.size
}