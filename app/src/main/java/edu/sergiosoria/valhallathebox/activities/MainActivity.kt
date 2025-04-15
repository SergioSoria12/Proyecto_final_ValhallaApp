package edu.sergiosoria.valhallathebox.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.sergiosoria.valhallathebox.R
import edu.sergiosoria.valhallathebox.utils.CurrentUser

class MainActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var ivProfile: ImageView
    private lateinit var tvWelcomeMessage: TextView
    private lateinit var upcomingClassesLayout: LinearLayout
    private lateinit var wodAccordionLayout: LinearLayout
    private lateinit var shopAccordionLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referencias
        tvUserName = findViewById(R.id.tvUserName)
        ivProfile = findViewById(R.id.ivProfileImage)
        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage)
        upcomingClassesLayout = findViewById(R.id.classList)
        wodAccordionLayout = findViewById(R.id.wodAccordion)
        shopAccordionLayout = findViewById(R.id.shopAccordion)

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

        // Navbar (opcional: navegación entre fragments o activities)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.nav_wod -> {
                    startActivity(Intent(this, WodActivity::class.java))
                    true
                }
                R.id.nav_schedule -> {
                    startActivity(Intent(this, HoraryActivity::class.java))
                    true
                }
                R.id.nav_shop -> {
                    startActivity(Intent(this, ShopActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Llamadas a cargar acordeones (lógica aún por implementar)
        loadUpcomingClasses()
        loadWods()
        loadShopProducts()
    }

    private fun loadUpcomingClasses() {
        // TODO: cargar clases desde Room
    }

    private fun loadWods() {
        // TODO: cargar WODs desde Room y añadir a wodAccordionLayout
    }

    private fun loadShopProducts() {
        // TODO: cargar productos desde Room y añadir a shopAccordionLayout
    }
}
