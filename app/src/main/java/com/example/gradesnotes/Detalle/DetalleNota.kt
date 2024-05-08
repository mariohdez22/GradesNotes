package com.example.gradesnotes.Detalle

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradesnotes.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DetalleNota : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var user: FirebaseUser

    private lateinit var botonImportante: Button
    private lateinit var idNotaD: TextView
    private lateinit var uidUsuarioD: TextView
    private lateinit var correoUsuarioD: TextView
    private lateinit var fechaRegistroD: TextView
    private lateinit var tituloD: TextView
    private lateinit var descripcionD: TextView
    private lateinit var fechaD: TextView
    private lateinit var estadoD: TextView

    private var comprobarNotaImportante = false

    private var idNotaR: String? = null
    private var uidUsuarioR: String? = null
    private var correoUsuarioR: String? = null
    private var fechaRegistroR: String? = null
    private var tituloR: String? = null
    private var descripcionR: String? = null
    private var fechaR: String? = null
    private var estadoR: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_nota)

        RecuperarDatos()
        InicializarVistas()
        SetearDatos()
        verificarNotaImportante()

        botonImportante.setOnClickListener {

            if (comprobarNotaImportante) {

                eliminarNotaImportante()

            } else {

                agregarNotaImportante()
            }
        }

    }

    private fun InicializarVistas() {

        idNotaD = findViewById(R.id.IdNotaD)
        uidUsuarioD = findViewById(R.id.UidUsuarioD)
        correoUsuarioD = findViewById(R.id.CorreoUsuarioD)
        fechaRegistroD = findViewById(R.id.FechaRegistroD)
        descripcionD = findViewById(R.id.DescripcionD)
        tituloD = findViewById(R.id.TituloNotaD)
        fechaD = findViewById(R.id.FechaNotaD)
        estadoD = findViewById(R.id.EstadoD)
        botonImportante = findViewById(R.id.Boton_Importante)

        firebaseAuth = FirebaseAuth.getInstance()
        user = firebaseAuth.currentUser!!
    }

    private fun RecuperarDatos(){

        val intent = intent.extras

        idNotaR = intent?.getString("id_nota")
        uidUsuarioR = intent?.getString("uid_usuario")
        correoUsuarioR = intent?.getString("correo_usuario")
        fechaRegistroR = intent?.getString("fecha_registro")
        tituloR = intent?.getString("titulo")
        descripcionR = intent?.getString("descripcion")
        fechaR = intent?.getString("fecha_nota")
        estadoR = intent?.getString("estado")
    }

    private fun SetearDatos() {
        idNotaD.text = idNotaR
        uidUsuarioD.text = uidUsuarioR
        correoUsuarioD.text = correoUsuarioR
        fechaRegistroD.text = fechaRegistroR
        tituloD.text = tituloR
        descripcionD.text = descripcionR
        fechaD.text = fechaR
        estadoD.text = estadoR
    }

    private fun verificarNotaImportante() {

        val intent = intent.extras
        idNotaR = intent?.getString("id_nota")

        val referencia = FirebaseDatabase.getInstance().getReference("Usuarios")
        referencia.child(firebaseAuth.uid!!).child("MisNotasImportantes").child(idNotaR ?: "")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    comprobarNotaImportante = snapshot.exists()
                    actualizarBotonImportante()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun actualizarBotonImportante() {
        if (comprobarNotaImportante) {
            botonImportante.text = "Importante"
            botonImportante.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.icono_nota_importante, 0, 0)
        } else {
            botonImportante.text = "No importante"
            botonImportante.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.icono_nota_no_importante, 0, 0)
        }
    }

    private fun agregarNotaImportante() {

        if (user == null) {
            Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = intent.extras

        idNotaR = intent?.getString("id_nota")
        uidUsuarioR = intent?.getString("uid_usuario")
        correoUsuarioR = intent?.getString("correo_usuario")
        fechaRegistroR = intent?.getString("fecha_registro")
        tituloR = intent?.getString("titulo")
        descripcionR = intent?.getString("descripcion")
        fechaR = intent?.getString("fecha_nota")
        estadoR = intent?.getString("estado")

        val notaImportante = hashMapOf(
            "idNota" to idNotaR,
            "uidUsuario" to uidUsuarioR,
            "correoUsuario" to correoUsuarioR,
            "fechaHoraActual" to fechaRegistroR,
            "titulo" to tituloR,
            "descripcion" to descripcionR,
            "fechaNota" to fechaR,
            "estado" to estadoR,
        )

        val referencia = FirebaseDatabase.getInstance().getReference("Usuarios")
        referencia.child(firebaseAuth.uid!!).child("MisNotasImportantes").child(idNotaR ?: "")
            .setValue(notaImportante)
            .addOnSuccessListener {

                Toast.makeText(this, "Se ha añadido a notas importantes", Toast.LENGTH_SHORT).show()

            }.addOnFailureListener {

                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarNotaImportante() {

        val intent = intent.extras
        idNotaR = intent?.getString("id_nota")

        val referencia = FirebaseDatabase.getInstance().getReference("Usuarios")
        referencia.child(user.uid).child("MisNotasImportantes").child(idNotaR ?: "")
            .removeValue()
            .addOnSuccessListener {

                Toast.makeText(this, "La nota ya no es importante", Toast.LENGTH_SHORT).show()

            }.addOnFailureListener {

                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

}