package edu.sergiosoria.valhallathebox.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import edu.sergiosoria.valhallathebox.R
import edu.sergiosoria.valhallathebox.utils.CurrentUser

class HomeFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var ivProfile: ImageView
    private lateinit var tvWelcomeMessage: TextView
    private lateinit var upcomingClassesLayout: LinearLayout
    private lateinit var wodAccordionLayout: LinearLayout
    private lateinit var shopAccordionLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Referencias
        tvUserName = view.findViewById(R.id.tvUserName)
        ivProfile = view.findViewById(R.id.ivProfileImage)
        tvWelcomeMessage = view.findViewById(R.id.tvWelcomeMessage)
        upcomingClassesLayout = view.findViewById(R.id.classList)
        wodAccordionLayout = view.findViewById(R.id.wodAccordion)
        shopAccordionLayout = view.findViewById(R.id.shopAccordion)

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
    }

    private fun loadUpcomingClasses() {
        // TODO: implementar
    }

    private fun loadWods() {
        // TODO: implementar
    }

    private fun loadShopProducts() {
        // TODO: implementar
    }
}