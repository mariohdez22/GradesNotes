package com.example.gradesnotes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var btnRegistro: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLogin = findViewById(R.id.BtnLogin)
        btnRegistro = findViewById(R.id.BtnRegister)

        btnLogin.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }

        btnRegistro.setOnClickListener {
            startActivity(Intent(this, Registro::class.java))
        }
    }
}