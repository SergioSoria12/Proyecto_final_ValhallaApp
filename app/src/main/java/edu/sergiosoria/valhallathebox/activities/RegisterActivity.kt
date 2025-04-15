package edu.sergiosoria.valhallathebox.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import edu.sergiosoria.valhallathebox.R
import edu.sergiosoria.valhallathebox.ValhallaApp
import edu.sergiosoria.valhallathebox.database.AppDatabase
import edu.sergiosoria.valhallathebox.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class RegisterActivity : AppCompatActivity() {

    private lateinit var avatarImageView: ImageView
    private lateinit var selectAvatarButton: Button
    private lateinit var typeButton: Button
    private var selectedUserType: Boolean = false // false = Coach, true = Atleta
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        typeButton = findViewById(R.id.btnType)
        selectAvatarButton = findViewById(R.id.btnSelectAvatar)
        avatarImageView = findViewById(R.id.ivAvatar)
        val name = findViewById<EditText>(R.id.etName)
        val surname = findViewById<EditText>(R.id.etSurname)
        val phone = findViewById<EditText>(R.id.etTelephone)
        val city = findViewById<EditText>(R.id.etCity)
        val email = findViewById<EditText>(R.id.etEmail)
        val password = findViewById<EditText>(R.id.etPassword)
        val confirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val registerButton = findViewById<Button>(R.id.btnRegister)

        //Seleccionamos tipo de usuario
       typeButton.setOnClickListener {
           showTypePickerDialog()
       }

        //Seleccionar imagen de avatar
        selectAvatarButton.setOnClickListener {
            showImagePickerDialog()
        }

        //Registrar nuevo usuario
        registerButton.setOnClickListener {
            Log.d("DB_TEST", "Click en botón de registro detectado")

            val passwordText = password.text.toString()
            val confirmPasswordText = confirmPassword.text.toString()

            if (passwordText != confirmPasswordText) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val avatarUriString = selectedImageUri?.toString() ?: ""

            val newUser = User(
                name = name.text.toString(),
                surname = surname.text.toString(),
                telephone = phone.text.toString(),
                city = city.text.toString(),
                email = email.text.toString(),
                password = passwordText,
                type = selectedUserType,
                avatar = avatarUriString
            )

            // GUARDAR EN BASE DE DATOS EN HILO SECUNDARIO
            CoroutineScope(Dispatchers.IO).launch {
                val userDao = ValhallaApp.database.userDao()
                userDao.insertUser(newUser)
                Log.d("DB_TEST", "Usuario insertado en Room: $newUser")
                // ⚠️ Recuperamos el usuario para ver su ID asignado
                val insertedUser = userDao.getUserByEmail(newUser.email)
                Log.d("DB_TEST", "ID del nuevo usuario: ${insertedUser?.id}")


                // SUBIR A FIREBASE
                val firebaseDb = FirebaseDatabase.getInstance("https://valhallathebox-default-rtdb.europe-west1.firebasedatabase.app/")
                val usersRef = firebaseDb.getReference("users")
                insertedUser?.let {
                    usersRef.child(it.id.toString()).setValue(it)
                        .addOnSuccessListener {
                            Log.d("FIREBASE", "Usuario subido a Firebase con ID ${insertedUser?.id}")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FIREBASE", "Error subiendo a Firebase: ${e.message}")
                        }
                }

                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun showTypePickerDialog() {
        val options = arrayOf("Coach", "Atleta")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona tu tipo de usuario")
        builder.setItems(options) { _, which ->
              selectedUserType = which == 1 //0 = Coach (false), 1 = Atleta (true)
              typeButton.text = options[which] // Cambia el texto del boton
        }
        builder.show()
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Tomar foto", "ELegir de la galería", "Cancelar")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecionar avatar")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> checkCameraPermission()
                1 -> checkGalleryPermission()
                2 -> dialog.dismiss()
            }
        }
        builder.show()
    }
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }else {
            openCamera()
        }
    }

    private fun checkGalleryPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), GALLERY_PERMISSION_CODE)
        } else {
            openGallery()
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == GALLERY_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery() // Si acepta, abre la galería
            } else {
                Toast.makeText(this, "Permiso denegado. No se puede acceder a la galería", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    // Activity Result para la galería
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val selectedUri = result.data!!.data
            if (selectedUri != null) {
                try {
                    val inputStream = contentResolver.openInputStream(selectedUri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val newUri = saveImageToInternalStorage(bitmap)
                    selectedImageUri = newUri
                    avatarImageView.setImageURI(newUri)
                } catch (e: Exception) {
                    Log.e("GALLERY", "Error al copiar imagen: ${e.message}")
                    Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //Guardar imagen convertida en Bitmap al almacenamiento interno
    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val filename = "avatar_${System.currentTimeMillis()}.jpg"
        val outputStream = openFileOutput(filename, Context.MODE_PRIVATE)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()
        val file = File(filesDir, filename)
        return Uri.fromFile(file)
    }


    // Activity Result para la cámara
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val bitmap = result.data!!.extras?.get("data") as Bitmap
            selectedImageUri = saveImageToGallery(bitmap)
            avatarImageView.setImageURI(selectedImageUri)
        }
    }

    // Guarda la imagen en la galería y devuelve su URI
    private fun saveImageToGallery(bitmap: Bitmap): Uri? {
        val imagePath = MediaStore.Images.Media.insertImage(
            contentResolver, bitmap, "Avatar_${System.currentTimeMillis()}", null
        )
        return Uri.parse(imagePath)
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val GALLERY_PERMISSION_CODE = 101
    }




}