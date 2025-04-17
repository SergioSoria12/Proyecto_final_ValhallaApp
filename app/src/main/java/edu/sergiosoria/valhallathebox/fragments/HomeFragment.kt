package edu.sergiosoria.valhallathebox.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import edu.sergiosoria.valhallathebox.R
import edu.sergiosoria.valhallathebox.ValhallaApp
import edu.sergiosoria.valhallathebox.activities.LoginActivity
import edu.sergiosoria.valhallathebox.database.AppDatabase
import edu.sergiosoria.valhallathebox.utils.CurrentUser
import edu.sergiosoria.valhallathebox.utils.getWodImageRes
import edu.sergiosoria.valhallathebox.utils.WodDetailBottomSheet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var ivProfile: ImageView
    private lateinit var tvWelcomeMessage: TextView
    private lateinit var wodAccordionLayout: LinearLayout
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = ValhallaApp.database

        // Referencias
        tvUserName = view.findViewById(R.id.tvUserName)
        ivProfile = view.findViewById(R.id.ivProfileImage)
        tvWelcomeMessage = view.findViewById(R.id.tvWelcomeMessage)
        wodAccordionLayout = view.findViewById(R.id.favoriteWodContainer)

        val user = CurrentUser.user

        // Mensaje de bienvenida
        tvWelcomeMessage.text = if (user?.type == true) "Bienvenido Atleta: " else "Bienvenido Coach: "

        // Nombre
        tvUserName.text = "${user?.name} ${user?.surname}"

        // Avatar
        try {
            val avatarUri = Uri.parse(user?.avatar)
            ivProfile.setImageURI(avatarUri)
        } catch (e: Exception) {
            Log.e("MAIN", "Error cargando avatar: ${e.message}")
        }

        // Acordeones
        loadUpcomingClasses()
        loadWods()
        loadShopProducts()

        val btnLogout = view.findViewById<ImageButton>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que quieres cerrar sesión?")
                .setPositiveButton("Sí") { _, _ ->
                    // Borrar usuario actual
                    CurrentUser.user = null

                    // Si usas token:
                    CoroutineScope(Dispatchers.IO).launch {
                        val prefs = requireContext().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
                        prefs.edit().remove("token").apply()
                        prefs.edit().remove("remember_user").apply()
                    }

                    // Volver a LoginActivity
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun loadUpcomingClasses() {
        // TODO: implementar
    }

    private fun loadWods() {
        val db = ValhallaApp.database
        viewLifecycleOwner.lifecycleScope.launch {
            db.wodDao().getAllWods().collect { fullWods ->
                val favoritos = fullWods.filter { it.wod.isFavorite }
                val container = view?.findViewById<LinearLayout>(R.id.favoriteWodContainer)
                container?.removeAllViews()

                val inflater = LayoutInflater.from(requireContext())
                favoritos.forEach { wodWithBlocks ->
                    val card = inflater.inflate(R.layout.item_wod_small, container, false)
                    val title = card.findViewById<TextView>(R.id.titleSmallWod)
                    val image = card.findViewById<ImageView>(R.id.imageSmallWod)

                    title.text = wodWithBlocks.wod.name
                    image.setImageResource(getWodImageRes(wodWithBlocks.wod.imageUri))

                    card.setOnClickListener {
                        WodDetailBottomSheet(wodWithBlocks).show(parentFragmentManager, "WodDetail")
                    }

                    container?.addView(card)
                }
            }
        }
    }

    private fun loadShopProducts() {
        // TODO: implementar
    }
}