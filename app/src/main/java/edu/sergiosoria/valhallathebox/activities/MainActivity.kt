package edu.sergiosoria.valhallathebox.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.sergiosoria.valhallathebox.R
import edu.sergiosoria.valhallathebox.fragments.HomeFragment
import edu.sergiosoria.valhallathebox.fragments.WodListFragment


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()

        // Navbar (opcional: navegación entre fragments o activities)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                    true
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                R.id.nav_wod -> {
                    // Ocultar main y mostrar fragmento
                    findViewById<ScrollView>(R.id.scrollContent).visibility = View.GONE
                    findViewById<FrameLayout>(R.id.fragment_container).visibility = View.VISIBLE

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, WodListFragment())
                        .addToBackStack(null)
                        .commit()
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


    }

}
