package com.example.gradesnotes

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Registro : AppCompatActivity() {

    private lateinit var nombreET: EditText
    private lateinit var correoET: EditText
    private lateinit var passwordET: EditText
    private lateinit var confirmPasswordET: EditText
    private lateinit var registrarUsuario: Button
    private lateinit var tengoCuentaTxt: TextView

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressBarDialog: AlertDialog

    private var nombre = ""
    private var correo = ""
    private var password = ""
    private var confirmarPassword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        val actionBar = supportActionBar
        actionBar?.title = "Registrar"
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        nombreET = findViewById(R.id.NombreET)
        correoET = findViewById(R.id.CorreoET)
        passwordET = findViewById(R.id.PasswordET)
        confirmPasswordET = findViewById(R.id.ConfirmPasswordET)
        registrarUsuario = findViewById(R.id.RegistrarUsuario)
        tengoCuentaTxt = findViewById(R.id.TengoCuentaTxt)

        firebaseAuth = FirebaseAuth.getInstance()

        //------------------------------------------------------------------------------------------

        val progressBar = ProgressBar(this)
        progressBar.isIndeterminate = true

        val builder = AlertDialog.Builder(this)
        builder.setView(progressBar)
        builder.setTitle("Espere por favor")
        builder.setCancelable(false) // para hacerlo no cancelable

        progressBarDialog = builder.create()

        //------------------------------------------------------------------------------------------

        registrarUsuario.setOnClickListener { view ->
            validarDatos()
        }

        tengoCuentaTxt.setOnClickListener{
            startActivity(Intent(this, Login::class.java))
        }
    }

    private fun validarDatos() {

        nombre = nombreET.text.toString()
        correo = correoET.text.toString()
        password = passwordET.text.toString()
        confirmarPassword = confirmPasswordET.text.toString()

        when {
            nombre.isEmpty() -> {
                Toast.makeText(this, "Ingrese nombre", Toast.LENGTH_SHORT).show()
            }
            !Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> {
                Toast.makeText(this, "Ingrese correo", Toast.LENGTH_SHORT).show()
            }
            password.isEmpty() -> {
                Toast.makeText(this, "Ingrese contraseña", Toast.LENGTH_SHORT).show()
            }
            confirmarPassword.isEmpty() -> {
                Toast.makeText(this, "Confirme contraseña", Toast.LENGTH_SHORT).show()
            }
            password != confirmarPassword -> {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            }
            else -> {
                crearCuenta()
            }
        }
    }

    private fun crearCuenta() {

        progressBarDialog.setMessage("Creando su cuenta...");
        showProgressBarDialog()

        firebaseAuth.createUserWithEmailAndPassword(correo, password)
            .addOnSuccessListener { authResult ->
                dismissProgressBarDialog()
                guardarInformacion()
            }
            .addOnFailureListener { e ->
                dismissProgressBarDialog()
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }

    }

    private fun guardarInformacion() {
        progressBarDialog.setMessage("Guardando su información")
        showProgressBarDialog()

        val uid = firebaseAuth.uid ?: ""

        val datos = hashMapOf(
            "uid" to uid,
            "correo" to correo,
            "nombres" to nombre,
            "password" to password,
            "apellidos" to "",
            "edad" to "",
            "telefono" to "",
            "domicilio" to "",
            "universidad" to "",
            "profesion" to "",
            "fecha_de_nacimiento" to "",
            "imagen_perfil" to ""
        )

        val databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios")
        databaseReference.child(uid).setValue(datos)
            .addOnSuccessListener {
                dismissProgressBarDialog()
                Toast.makeText(this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MenuPrincipal::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                dismissProgressBarDialog()
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun showProgressBarDialog() {
        progressBarDialog.show()
    }

    private fun dismissProgressBarDialog() {
        if (progressBarDialog.isShowing) {
            progressBarDialog.dismiss()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }

}