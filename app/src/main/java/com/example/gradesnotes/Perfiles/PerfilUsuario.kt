package com.example.gradesnotes.Perfiles

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.gradesnotes.ActualizarPass.ActualizarPassUsuario
import com.example.gradesnotes.MenuPrincipal
import com.example.gradesnotes.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hbb20.CountryCodePicker
import java.util.Calendar

class PerfilUsuario : AppCompatActivity() {

    private lateinit var imagenPerfil: ImageView
    private lateinit var correoPerfil: TextView
    private lateinit var uidPerfil: TextView
    private lateinit var telefonoPerfil: TextView
    private lateinit var fechaNacimientoPerfil: TextView
    private lateinit var nombresPerfil: EditText
    private lateinit var nombrePerfil: TextView
    private lateinit var apellidosPerfil: EditText
    private lateinit var edadPerfil: EditText
    private lateinit var domicilioPerfil: EditText
    private lateinit var universidadPerfil: EditText
    private lateinit var profesionPerfil: EditText

    private lateinit var editarTelefono: ImageView
    private lateinit var editarFecha: ImageView
    private lateinit var editarImagen: CardView

    private lateinit var guardarDatos: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var usuariosRef: DatabaseReference

    private lateinit var dialogEstablecerTelefono: Dialog

    private var dia: Int = 0
    private var mes: Int = 0
    private var anio: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_usuario)

        val toolbar = findViewById<Toolbar>(R.id.PassToolbar)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        actionBar?.title = "Perfil de usuario"
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        inicializarVariables()

        editarTelefono.setOnClickListener {
            establecerTelefonoUsuario()
        }

        editarFecha.setOnClickListener {
            abrirCalendario()
        }

        guardarDatos.setOnClickListener {
            actualizarDatos()
        }

        editarImagen.setOnClickListener {
            startActivity(Intent(this@PerfilUsuario, EditarImagenPerfil::class.java))
        }

    }

    private fun inicializarVariables() {
        imagenPerfil = findViewById(R.id.Imagen_Perfil)
        correoPerfil = findViewById(R.id.Correo_Perfil)
        uidPerfil = findViewById(R.id.Uid_Perfil)
        nombresPerfil = findViewById(R.id.Nombres_Perfil)
        nombrePerfil = findViewById(R.id.Nombre_Perfil)
        apellidosPerfil = findViewById(R.id.Apellidos_Perfil)
        edadPerfil = findViewById(R.id.Edad_Perfil)
        telefonoPerfil = findViewById(R.id.Telefono_Perfil)
        domicilioPerfil = findViewById(R.id.Domicilio_Perfil)
        universidadPerfil = findViewById(R.id.Universidad_Perfil)
        profesionPerfil = findViewById(R.id.Profesion_Perfil)
        fechaNacimientoPerfil = findViewById(R.id.Fecha_Nacimiento_Perfil)

        editarTelefono = findViewById(R.id.Editar_Telefono)
        editarFecha = findViewById(R.id.Editar_fecha)
        editarImagen = findViewById(R.id.Editar_imagen)

        dialogEstablecerTelefono = Dialog(this)

        guardarDatos = findViewById(R.id.Guardar_Datos)

        firebaseAuth = FirebaseAuth.getInstance()
        user = firebaseAuth.currentUser!!
        usuariosRef = FirebaseDatabase.getInstance().getReference("Usuarios")
    }

    private fun lecturaDeDatos() {
        usuariosRef.child(user.uid ?: "").addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    // Obtener sus datos
                    val uid = snapshot.child("uid").getValue(String::class.java) ?: ""
                    val nombre = snapshot.child("nombres").getValue(String::class.java) ?: ""
                    val apellidos = snapshot.child("apellidos").getValue(String::class.java) ?: ""
                    val correo = snapshot.child("correo").getValue(String::class.java) ?: ""
                    val edad = snapshot.child("edad").getValue(String::class.java) ?: ""
                    val telefono = snapshot.child("telefono").getValue(String::class.java) ?: ""
                    val domicilio = snapshot.child("domicilio").getValue(String::class.java) ?: ""
                    val universidad = snapshot.child("universidad").getValue(String::class.java) ?: ""
                    val profesion = snapshot.child("profesion").getValue(String::class.java) ?: ""
                    val fechaNacimiento = snapshot.child("fecha_de_nacimiento").getValue(String::class.java) ?: ""
                    val imagenPerfil = snapshot.child("imagen_perfil").getValue(String::class.java) ?: ""

                    // Seteo de datos
                    uidPerfil.text = uid
                    nombrePerfil.text = nombre
                    nombresPerfil.setText(nombre)
                    apellidosPerfil.setText(apellidos)
                    correoPerfil.text = correo
                    edadPerfil.setText(edad)
                    telefonoPerfil.text = telefono
                    domicilioPerfil.setText(domicilio)
                    universidadPerfil.setText(universidad)
                    profesionPerfil.setText(profesion)
                    fechaNacimientoPerfil.text = fechaNacimiento

                    cargarImagen(imagenPerfil)

                } else {
                    Toast.makeText(this@PerfilUsuario, "Esperando datos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PerfilUsuario, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun cargarImagen(imagenPerfils: String?) {
        try {
            // Cuando la imagen ha sido traída exitosamente desde Firebase
            Glide.with(applicationContext)
                .load(imagenPerfils)
                .placeholder(R.drawable.imagen_perfil_usuario)
                .into(imagenPerfil)
        } catch (e: Exception) {
            // Si la imagen no fue traída con éxito
            Glide.with(applicationContext)
                .load(R.drawable.imagen_perfil_usuario)
                .into(imagenPerfil)
        }
    }

    private fun establecerTelefonoUsuario() {
        val ccp: CountryCodePicker
        val establecerTelefono: EditText
        val btnAceptarTelefono: Button

        dialogEstablecerTelefono.setContentView(R.layout.cuadro_dialogo_establecer_telefono)

        ccp = dialogEstablecerTelefono.findViewById(R.id.ccp)
        establecerTelefono = dialogEstablecerTelefono.findViewById(R.id.Establecer_Telefono)
        btnAceptarTelefono = dialogEstablecerTelefono.findViewById(R.id.Btn_Aceptar_Telefono)

        btnAceptarTelefono.setOnClickListener {
            val codigoPais = ccp.selectedCountryCodeWithPlus
            val telefono = establecerTelefono.text.toString()
            val codigoPaisTelefono = "$codigoPais$telefono" // +51956605043

            if (telefono.isNotEmpty()) {
                telefonoPerfil.text = codigoPaisTelefono
                dialogEstablecerTelefono.dismiss()
            } else {
                Toast.makeText(this@PerfilUsuario, "Ingrese un número telefónico", Toast.LENGTH_SHORT).show()
                dialogEstablecerTelefono.dismiss()
            }
        }

        dialogEstablecerTelefono.show()
        dialogEstablecerTelefono.setCanceledOnTouchOutside(true)
    }

    private fun abrirCalendario() {
        val calendario = Calendar.getInstance()

        dia = calendario.get(Calendar.DAY_OF_MONTH)
        mes = calendario.get(Calendar.MONTH)
        anio = calendario.get(Calendar.YEAR)

        val datePickerDialog = DatePickerDialog(this, { _, anioSeleccionado, mesSeleccionado, diaSeleccionado ->
            val diaFormateado = if (diaSeleccionado < 10) "0$diaSeleccionado" else "$diaSeleccionado"
            val mesFormateado = if (mesSeleccionado + 1 < 10) "0${mesSeleccionado + 1}" else "${mesSeleccionado + 1}"

            fechaNacimientoPerfil.text = "$diaFormateado/$mesFormateado/$anioSeleccionado"
        }, anio, mes, dia)

        datePickerDialog.show()
    }

    private fun actualizarDatos() {
        val aNombre = nombresPerfil.text.toString().trim()
        val aApellidos = apellidosPerfil.text.toString().trim()
        val aEdad = edadPerfil.text.toString().trim()
        val aTelefono = telefonoPerfil.text.toString().trim()
        val aDomicilio = domicilioPerfil.text.toString()
        val aUniversidad = universidadPerfil.text.toString()
        val aProfesion = profesionPerfil.text.toString()
        val aFechaNacimiento = fechaNacimientoPerfil.text.toString()

        val datosActualizar = mapOf(
            "nombres" to aNombre as Any,
            "apellidos" to aApellidos as Any,
            "edad" to aEdad as Any,
            "telefono" to aTelefono as Any,
            "domicilio" to aDomicilio as Any,
            "universidad" to aUniversidad as Any,
            "profesion" to aProfesion as Any,
            "fecha_de_nacimiento" to aFechaNacimiento as Any
        )

        usuariosRef.child(user?.uid ?: "").updateChildren(datosActualizar)
            .addOnSuccessListener {
                Toast.makeText(this, "Actualizado correctamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_actualizar_pass, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.Actualizar_pass) {
            startActivity(Intent(this, ActualizarPassUsuario::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun comprobarInicioSesion() {
        if (user != null) {
            lecturaDeDatos()
        } else {
            startActivity(Intent(this, MenuPrincipal::class.java))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        comprobarInicioSesion()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

}