package edu.sergiosoria.valhallathebox.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.sergiosoria.valhallathebox.R
import edu.sergiosoria.valhallathebox.fragments.HomeFragment
import edu.sergiosoria.valhallathebox.fragments.ProfileFragment
import edu.sergiosoria.valhallathebox.fragments.ScheduleFragment
import edu.sergiosoria.valhallathebox.fragments.ShopFragment
import edu.sergiosoria.valhallathebox.fragments.WodListFragment
import edu.sergiosoria.valhallathebox.utils.SyncUtils
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Fragmento inicial
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()

        // 🔁 Sincronización con Firebase
        lifecycleScope.launch {
            SyncUtils.syncAllToFirebase() // Room → Firebase
        }
        SyncUtils.syncFirebaseToRoom(this) // Firebase → Room

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
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment())
                        .commit()
                    true
                }
                R.id.nav_wod -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, WodListFragment())
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.nav_schedule -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ScheduleFragment())
                        .commit()
                    true
                }
                R.id.nav_shop -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ShopFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }
}