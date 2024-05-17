package com.example.gradesnotes

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var CorreoLogin: EditText
    private lateinit var PassLogin: EditText
    private lateinit var Btn_Logeo: Button
    private lateinit var UsuarioNuevoTxt: TextView

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressBarDialog: AlertDialog

    private var correo = ""
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val actionBar = supportActionBar
        actionBar?.title = "Iniciar Sesion"
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        CorreoLogin = findViewById(R.id.CorreoLogin)
        PassLogin = findViewById(R.id.PassLogin)
        Btn_Logeo = findViewById(R.id.BtnLogin)
        UsuarioNuevoTxt = findViewById(R.id.UsuarioNuevoTxt)

        firebaseAuth = FirebaseAuth.getInstance()

        //------------------------------------------------------------------------------------------



        //------------------------------------------------------------------------------------------

        Btn_Logeo.setOnClickListener { view ->
            validarDatos()
        }

        UsuarioNuevoTxt.setOnClickListener{
            startActivity(Intent(this, Registro::class.java))
        }

    }

    private fun validarDatos() {

        correo = CorreoLogin.text.toString()
        password = PassLogin.text.toString()

        when {
            !Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> {
                Toast.makeText(this, "Ingrese correo", Toast.LENGTH_SHORT).show()
            }
            password.isEmpty() -> {
                Toast.makeText(this, "Ingrese contraseña", Toast.LENGTH_SHORT).show()
            }
            else -> {
                loginDeUsuario()
            }
        }
    }

    private fun loginDeUsuario()
    {
        firebaseAuth.signInWithEmailAndPassword(correo, password)
            .addOnCompleteListener{ authResult ->

                if (authResult.isSuccessful) {

                    val user = firebaseAuth.currentUser!!
                    startActivity(Intent(this, MenuPrincipal::class.java))
                    finish()
                }
                else{

                    Toast.makeText(this, "Verifique si el correo y contraseña son los correctos", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }

}