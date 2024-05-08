package com.example.gradesnotes.ActualizarPass

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.gradesnotes.Login
import com.example.gradesnotes.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ActualizarPassUsuario : AppCompatActivity() {

    private lateinit var actualPass: TextView
    private lateinit var etActualPass: EditText
    private lateinit var etNuevoPass: EditText
    private lateinit var etRNuevoPass: EditText
    private lateinit var btnActualizarPass: Button

    private lateinit var bdUsuarios: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var user: FirebaseUser

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actualizar_pass_usuario)

        val actionBar = supportActionBar
        actionBar?.title = "Cambiar contraseña"
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        inicializarVariables()
        lecturaDeDato()

        btnActualizarPass.setOnClickListener {
            val passActual = etActualPass.text.toString().trim()
            val nuevoPass = etNuevoPass.text.toString().trim()
            val rNuevoPass = etRNuevoPass.text.toString().trim()

            when {
                passActual.isEmpty() -> etActualPass.error = "Llene el campo"
                nuevoPass.isEmpty() -> etNuevoPass.error = "Llene el campo"
                rNuevoPass.isEmpty() -> etRNuevoPass.error = "Llene el campo"
                nuevoPass != rNuevoPass -> Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                nuevoPass.length < 6 -> etNuevoPass.error = "Debe ser igual o mayor de 6 caracteres"
                else -> actualizarPassword(passActual, nuevoPass)
            }
        }

    }

    private fun actualizarPassword(passActual: String, nuevoPass: String) {

        progressDialog.apply {
            show()
            title = "Actualizando"
            setMessage("Espere por favor")
        }

        val authCredential = EmailAuthProvider.getCredential(user.email!!, passActual)
        user.reauthenticate(authCredential)
            .addOnSuccessListener {
                user.updatePassword(nuevoPass)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        val nuevoPassTrim = etNuevoPass.text.toString().trim()
                        val hashMap = hashMapOf<String, Any>("password" to nuevoPassTrim)
                        val bdUsuarios = FirebaseDatabase.getInstance().getReference("Usuarios")
                        bdUsuarios.child(user.uid).updateChildren(hashMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Contraseña cambiada con éxito", Toast.LENGTH_SHORT).show()
                                firebaseAuth.signOut()
                                val intent = Intent(this, Login::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                progressDialog.dismiss()
                                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "La contraseña actual no es la correcta", Toast.LENGTH_SHORT).show()
            }
    }

    private fun inicializarVariables() {
        actualPass = findViewById(R.id.ActualPass)
        etActualPass = findViewById(R.id.EtActualPass)
        etNuevoPass = findViewById(R.id.EtNuevoPass)
        etRNuevoPass = findViewById(R.id.EtRNuevoPass)
        btnActualizarPass = findViewById(R.id.BtnActualizarPass)

        firebaseAuth = FirebaseAuth.getInstance()
        user = firebaseAuth.currentUser ?: throw IllegalStateException("Usuario no disponible")

        progressDialog = ProgressDialog(this).apply {
            setTitle("Cargando...")
            setCancelable(false)
        }
    }

    private fun lecturaDeDato() {
        val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
        reference.child(user.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pass = snapshot.child("password").getValue(String::class.java) ?: ""
                actualPass.text = pass
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ActualizarPassUsuario, "Error al cargar datos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}