package edu.sergiosoria.valhallathebox.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.sergiosoria.valhallathebox.R
import edu.sergiosoria.valhallathebox.models.Wod
import edu.sergiosoria.valhallathebox.utils.getWodImageRes

class WodAdapter(
    private val wodList: List<Wod>,
    private val onWodClick: (Wod) -> Unit,
    private val onFavoriteClick: (Wod) -> Unit // Nuevo parámetro
) : RecyclerView.Adapter<WodAdapter.WodViewHolder>() {

    inner class WodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imageWod)
        val title: TextView = view.findViewById(R.id.titleWod)
        val favorite: ImageView = view.findViewById(R.id.ivFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wod, parent, false)
        return WodViewHolder(view)
    }

    override fun onBindViewHolder(holder: WodViewHolder, position: Int) {
        val wod = wodList[position]

        holder.title.text = wod.name

        // Imagen
        holder.image.setImageResource(getWodImageRes(wod.imageUri))

        // Animación
        holder.itemView.alpha = 0f
        holder.itemView.animate().alpha(1f).setDuration(500).start()

        // Click en la tarjeta
        holder.itemView.setOnClickListener {
            onWodClick(wod)
        }

        // Estado de la estrella
        holder.favorite.setImageResource(
            if (wod.isFavorite) R.drawable.ic_star else R.drawable.ic_star_border
        )

        // Click en la estrella
        holder.favorite.setOnClickListener {
            onFavoriteClick(wod)
        }
    }

    override fun getItemCount(): Int = wodList.size
}