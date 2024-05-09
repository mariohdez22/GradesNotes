package com.example.gradesnotes.AgregarNotas

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.gradesnotes.DrawNota.DrawView
import com.example.gradesnotes.DrawNota.StrokeManager.clear
import com.example.gradesnotes.DrawNota.StrokeManager.download
import com.example.gradesnotes.DrawNota.StrokeManager.recognize
import com.example.gradesnotes.Modelos.Nota
import com.example.gradesnotes.R
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AgregarNota : AppCompatActivity() {

    private lateinit var uidUsuario: TextView
    private lateinit var correoUsuario: TextView
    private lateinit var fechaActual: TextView
    private lateinit var fecha: TextView
    private lateinit var estado: TextView
    private lateinit var navegacion : NavigationBarView
    private lateinit var linearPendiente : LinearLayout
    private lateinit var linearDraw: LinearLayout
    private lateinit var scrollNota: ScrollView
    private var menu: Menu? = null

    private lateinit var titulo: EditText
    private lateinit var descripcion: EditText
    private lateinit var drawView: DrawView

    private lateinit var dbFirebase:DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_nota)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "Agregar Nota"
            titleColor = resources.getColor(R.color.black800)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        inicializarVariables()
        obtenerDatos()
        obtenerFechaHoraActual()

        download()
    }

    private fun inicializarVariables() {
        uidUsuario = findViewById(R.id.UidUsuario)
        correoUsuario = findViewById(R.id.CorreoUsuario)
        fechaActual = findViewById(R.id.FechaActual)
        fecha = findViewById(R.id.Fecha)
        estado = findViewById(R.id.Estado)
        navegacion = findViewById(R.id.navmenu)
        navegacion.setOnItemSelectedListener(opcionMenuSeleccionado)
        linearPendiente = findViewById(R.id.linearPendiente)
        linearDraw = findViewById(R.id.linearDraw)
        scrollNota = findViewById(R.id.scrollNota)

        titulo = findViewById(R.id.Titulo)
        descripcion = findViewById(R.id.Descripcion)

        drawView = findViewById(R.id.draw_view)

        dbFirebase = FirebaseDatabase.getInstance().reference
    }

    private fun obtenerDatos() {
        val uidRecuperado = intent.getStringExtra("Uid")
        val correoRecuperado = intent.getStringExtra("Correo")

        uidUsuario.text = uidRecuperado
        correoUsuario.text = correoRecuperado
    }

    private fun obtenerFechaHoraActual() {
        val fechaHoraRegistro = SimpleDateFormat("dd MMM yyyy h:mm a", Locale.getDefault()).format(System.currentTimeMillis())
        fechaActual.text = fechaHoraRegistro
    }

    private fun agregarNota() {

        // Obtener los datos
        val idNota = dbFirebase.push().key ?: return  // Terminar si getKey es nulo
        val uidUsuario = uidUsuario.text.toString()
        val correoUsuario = correoUsuario.text.toString()
        val fechaHoraActual = fechaActual.text.toString()
        val titulo = titulo.text.toString()
        val descripcion = descripcion.text.toString()
        val fecha = fecha.text.toString()
        val estados: String

        if (fecha.isEmpty())
            estados = ""
        else
            estados = estado.text.toString()

        // Validar datos
        if (uidUsuario.isNotEmpty() && correoUsuario.isNotEmpty() && fechaHoraActual.isNotEmpty() &&
            titulo.isNotEmpty() && descripcion.isNotEmpty()) {

            val nota = Nota(
                idNota = idNota,
                uidUsuario = uidUsuario,
                correoUsuario = correoUsuario,
                fechaHoraActual = fechaHoraActual,
                titulo = titulo,
                descripcion = descripcion,
                fechaNota = fecha,
                estado = estados
            )

            // Establecer el nombre de la BD
            val nombreBD = "NotasPublicadas"

            dbFirebase.child(nombreBD).child(idNota).setValue(nota)
                .addOnSuccessListener {
                    Toast.makeText(this, "Se ha agregado la nota exitosamente", Toast.LENGTH_SHORT).show()
                    onBackPressedDispatcher.onBackPressed()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al agregar la nota: ${e.message}", Toast.LENGTH_SHORT).show()
                }

        } else {
            Toast.makeText(this, "Llenar todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater: MenuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_agenda, menu)
        this.menu = menu
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.Agregar_Nota_BD -> {
                agregarNota()
                return true
            }
            R.id.ExtraerTexto -> {

                menu?.findItem(R.id.Agregar_Nota_BD)?.isVisible = true
                menu?.findItem(R.id.ExtraerTexto)?.isVisible = false
                menu?.findItem(R.id.LimpiarDibujo)?.isVisible = false

                val layoutParams = scrollNota.layoutParams
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                scrollNota.layoutParams = layoutParams

                descripcion.visibility = View.VISIBLE
                linearDraw.visibility = View.GONE

                recognize(
                    descripcion
                )

                drawView.clear()
                clear()

                return true
            }
            R.id.LimpiarDibujo -> {
                drawView.clear()
                clear()
                descripcion.setText("")
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }

    private val opcionMenuSeleccionado = NavigationBarView.OnItemSelectedListener{

        item -> when(item.itemId){

            R.id.calendario -> {

                val calendario = Calendar.getInstance()
                val dia = calendario.get(Calendar.DAY_OF_MONTH)
                val mes = calendario.get(Calendar.MONTH)
                val anio = calendario.get(Calendar.YEAR)

                val datePickerDialog = DatePickerDialog(this, { _, anioSeleccionado, mesSeleccionado, diaSeleccionado ->
                    val diaFormateado = if (diaSeleccionado < 10) "0$diaSeleccionado" else "$diaSeleccionado"
                    val mesAjustado = mesSeleccionado + 1
                    val mesFormateado = if (mesAjustado < 10) "0$mesAjustado" else "$mesAjustado"

                    linearPendiente.visibility = View.VISIBLE
                    fecha.text = "$diaFormateado/$mesFormateado/$anioSeleccionado"
                }, anio, mes, dia)

                datePickerDialog.show()
            }
            R.id.listado -> {

                Toast.makeText(this, "Listado", Toast.LENGTH_SHORT).show()
            }
            R.id.painter -> {

                menu?.findItem(R.id.Agregar_Nota_BD)?.isVisible = false
                menu?.findItem(R.id.ExtraerTexto)?.isVisible = true
                menu?.findItem(R.id.LimpiarDibujo)?.isVisible = true

                val layoutParams = scrollNota.layoutParams
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                scrollNota.layoutParams = layoutParams

                descripcion.visibility = View.GONE
                linearDraw.visibility = View.VISIBLE
            }
            R.id.extractor -> {

                Toast.makeText(this, "Extractor texto", Toast.LENGTH_SHORT).show()
            }
        }
        false
    }

}