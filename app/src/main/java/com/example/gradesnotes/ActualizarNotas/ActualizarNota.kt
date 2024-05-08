package com.example.gradesnotes.ActualizarNotas

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.gradesnotes.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class ActualizarNota : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    // Declaración de las vistas utilizando lateinit para inicializar más tarde
    private lateinit var idNotaA: TextView
    private lateinit var uidUsuarioA: TextView
    private lateinit var correoUsuarioA: TextView
    private lateinit var fechaRegistroA: TextView
    private lateinit var fechaA: TextView
    private lateinit var estadoA: TextView
    private lateinit var estadoNuevo: TextView

    private lateinit var tituloA: EditText
    private lateinit var descripcionA: EditText

    private lateinit var btnCalendarioA: Button

    // ImageView para indicar el estado de la tarea
    private lateinit var tareaFinalizada: ImageView
    private lateinit var tareaNoFinalizada: ImageView

    // Spinner para seleccionar el nuevo estado
    private lateinit var spinnerEstado: Spinner

    // Variables para almacenar los datos recuperados de la actividad anterior
    private var idNotaR: String? = null
    private var uidUsuarioR: String? = null
    private var correoUsuarioR: String? = null
    private var fechaRegistroR: String? = null
    private var tituloR: String? = null
    private var descripcionR: String? = null
    private var fechaR: String? = null
    private var estadoR: String? = null

    // Variables para manejar la fecha
    private var dia: Int = 0
    private var mes: Int = 0
    private var anio: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actualizar_nota)

        val actionBar = supportActionBar
        actionBar?.title = "Actualizar Nota"
        actionBar?.setDisplayShowHomeEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val toolbar = findViewById<Toolbar>(R.id.EditToolbar)
        setSupportActionBar(toolbar)

        InicializarVariables()
        RecuperarDatos()
        SetearDatos()
        ComprobarEstadoNota()
        SpinnerEstado()
        btnCalendarioA.setOnClickListener {

            SeleccionarFecha()
        }

    }

    private fun InicializarVariables(){

        idNotaA = findViewById(R.id.Id_nota_A)
        uidUsuarioA = findViewById(R.id.Uid_Usuario_A)
        correoUsuarioA = findViewById(R.id.Correo_usuario_A)
        fechaRegistroA = findViewById(R.id.Fecha_registro_A)
        fechaA = findViewById(R.id.Fecha_A)
        estadoA = findViewById(R.id.Estado_A)
        estadoNuevo = findViewById(R.id.Estado_nuevo)

        tituloA = findViewById(R.id.Titulo_A)
        descripcionA = findViewById(R.id.Descripcion_A)

        btnCalendarioA = findViewById(R.id.Btn_Calendario_A)

        tareaFinalizada = findViewById(R.id.Tarea_Finalizada)
        tareaNoFinalizada = findViewById(R.id.Tarea_No_Finalizada)

        spinnerEstado = findViewById(R.id.Spinner_estado)

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

        idNotaA.text = idNotaR
        uidUsuarioA.text = uidUsuarioR
        correoUsuarioA.text = correoUsuarioR
        fechaRegistroA.text = fechaRegistroR
        tituloA.setText(tituloR)
        descripcionA.setText(descripcionR)
        fechaA.text = fechaR
        estadoA.text = estadoR
    }

    private fun ComprobarEstadoNota() {
        val estadoNota = estadoA.text.toString()

        when (estadoNota) {
            "No finalizado" -> tareaNoFinalizada.visibility = View.VISIBLE
            "Finalizado" -> tareaFinalizada.visibility = View.VISIBLE
        }
    }

    private fun SeleccionarFecha(){

        val calendario = Calendar.getInstance()
        val dia = calendario.get(Calendar.DAY_OF_MONTH)
        val mes = calendario.get(Calendar.MONTH)
        val anio = calendario.get(Calendar.YEAR)

        val datePickerDialog = DatePickerDialog(this, { _, anioSeleccionado, mesSeleccionado, diaSeleccionado ->
            val diaFormateado = if (diaSeleccionado < 10) "0$diaSeleccionado" else "$diaSeleccionado"
            val mesAjustado = mesSeleccionado + 1
            val mesFormateado = if (mesAjustado < 10) "0$mesAjustado" else "$mesAjustado"

            fechaA.text = "$diaFormateado/$mesFormateado/$anioSeleccionado"
        }, anio, mes, dia)

        datePickerDialog.show()
    }

    private fun SpinnerEstado() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.Estados_nota,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEstado.adapter = adapter
        spinnerEstado.onItemSelectedListener = this
    }

    private fun ActualizarNotaDB(){

        val tituloActualizar = tituloA.text.toString()
        val descripcionActualizar = descripcionA.text.toString()
        val fechaActualizar = fechaA.text.toString()
        val estadoActualizar = estadoNuevo.text.toString()

        val firebaseDatabase = FirebaseDatabase.getInstance()
        val databaseReference =firebaseDatabase.getReference("NotasPublicadas")

        //Consulta
        val query: Query = databaseReference.orderByChild("idNota").equalTo(idNotaR)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    ds.ref.child("titulo").setValue(tituloActualizar)
                    ds.ref.child("descripcion").setValue(descripcionActualizar)
                    ds.ref.child("fechaNota").setValue(fechaActualizar)
                    ds.ref.child("estado").setValue(estadoActualizar)
                }
                Toast.makeText(
                    this@ActualizarNota,
                    "Nota actualizada con éxito",
                    Toast.LENGTH_SHORT
                ).show()
                onBackPressedDispatcher.onBackPressed()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }

    override fun onItemSelected(adapterView: AdapterView<*>, view: View?, i: Int, l: Long) {

        val ESTADO_ACTUAL: String = estadoA.text.toString()
        val Posicion_1 = adapterView.getItemAtPosition(1).toString()
        val estado_seleccionado = adapterView.getItemAtPosition(i).toString()
        estadoNuevo.text = estado_seleccionado

        if (ESTADO_ACTUAL == "Finalizado") {
            estadoNuevo.text = Posicion_1
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_actualizar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.Actualizar_Nota_BD -> ActualizarNotaDB()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }

}