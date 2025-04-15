package edu.sergiosoria.valhallathebox.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import edu.sergiosoria.valhallathebox.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Forzamos pantalla completa
        window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                        android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )
        supportActionBar?.hide()

        setContentView(R.layout.activity_splash)

        val videoView = findViewById<VideoView>(R.id.videoView)

        // Cargar y reproducir el video
        val videoPath = "android.resource://" + packageName + "/" + R.raw.entrada_app
        videoView.setVideoURI(Uri.parse(videoPath))
        videoView.setOnCompletionListener {
            // Ir a la pantalla de Login cuando termine el video
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Cierra esta pantalla para que no vuelva atrás
        }

        videoView.start()
    }
}