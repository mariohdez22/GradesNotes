package com.example.gradesnotes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth

class PantallaCarga : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_carga)

        firebaseAuth = FirebaseAuth.getInstance()

        val tiempo: Long = 3000

        Handler(Looper.getMainLooper()).postDelayed({
            verificarUsuario()
        }, tiempo)

    }

    private fun verificarUsuario() {

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, MenuPrincipal::class.java))
        }
        finish()
    }

}