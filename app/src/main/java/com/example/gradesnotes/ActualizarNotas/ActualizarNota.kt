package com.example.gradesnotes.ActualizarNotas

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.example.gradesnotes.DrawNota.DrawView
import com.example.gradesnotes.DrawNota.StrokeManager
import com.example.gradesnotes.DrawNota.StrokeManager.download
import com.example.gradesnotes.R
import com.google.android.material.navigation.NavigationBarView
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
    private lateinit var navegacion : NavigationBarView
    private lateinit var estadoNuevo: TextView
    private lateinit var linearPendiente: LinearLayout
    private lateinit var linearDraw: LinearLayout
    private lateinit var scrollNota: ScrollView
    private var menu: Menu? = null
    private var menu2: Menu? = null

    private lateinit var tituloA: EditText
    private lateinit var descripcionA: EditText
    private lateinit var drawView: DrawView

    // ImageView para indicar el estado de la tarea
    private lateinit var tareaFinalizada: CardView
    private lateinit var tareaNoFinalizada: CardView

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

        val toolbar = findViewById<Toolbar>(R.id.EditToolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "Editar Nota"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        InicializarVariables()
        RecuperarDatos()
        SetearDatos()
        ComprobarEstadoNota()
        SpinnerEstado()

        download()
    }

    //----------------------------------------------------------------------------------------------

    private fun InicializarVariables(){

        idNotaA = findViewById(R.id.Id_nota_A)
        uidUsuarioA = findViewById(R.id.Uid_Usuario_A)
        correoUsuarioA = findViewById(R.id.Correo_usuario_A)
        fechaRegistroA = findViewById(R.id.Fecha_registro_A)
        fechaA = findViewById(R.id.Fecha_A)
        estadoA = findViewById(R.id.Estado_A)
        estadoNuevo = findViewById(R.id.Estado_nuevo)
        linearPendiente = findViewById(R.id.linearPendiente)
        linearDraw = findViewById(R.id.linearDraw)
        scrollNota = findViewById(R.id.scrollNota)

        navegacion = findViewById(R.id.navmenu)
        navegacion.setOnItemSelectedListener(opcionMenuSeleccionado)

        tituloA = findViewById(R.id.Titulo_A)
        descripcionA = findViewById(R.id.Descripcion_A)

        drawView = findViewById(R.id.draw_view)

        tareaFinalizada = findViewById(R.id.Tarea_Finalizada)
        tareaNoFinalizada = findViewById(R.id.Tarea_No_Finalizada)

        spinnerEstado = findViewById(R.id.Spinner_estado)

    }

    //----------------------------------------------------------------------------------------------

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

    //----------------------------------------------------------------------------------------------

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

    //----------------------------------------------------------------------------------------------

    private fun ComprobarEstadoNota() {

        val estadoNota = estadoA.text.toString()

        if (estadoNota.isNotEmpty()) {

            linearPendiente.visibility = View.VISIBLE

            when (estadoNota) {
                "pendiente" -> tareaNoFinalizada.visibility = View.VISIBLE
                "finalizado" -> tareaFinalizada.visibility = View.VISIBLE
            }

        }else{

            linearPendiente.visibility = View.GONE
        }
    }

    //----------------------------------------------------------------------------------------------

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

    //----------------------------------------------------------------------------------------------

    private fun SpinnerEstado() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.Estados_nota,
            R.layout.spinner_item
        )
        adapter.setDropDownViewResource(R.layout.spinner_item)
        spinnerEstado.adapter = adapter
        spinnerEstado.onItemSelectedListener = this

        val estadoActual : String = estadoA.text.toString()
        val estados = resources.getStringArray(R.array.Estados_nota)
        spinnerEstado.setSelection(estados.indexOf(estadoActual))
    }

    //----------------------------------------------------------------------------------------------

    private fun ActualizarNotaDB(){

        val tituloActualizar = tituloA.text.toString()
        val descripcionActualizar = descripcionA.text.toString()
        val fechaActualizar = fechaA.text.toString()
        val estadoActualizar: String

        if(estadoA.text.isEmpty())
            estadoActualizar = ""
        else
            estadoActualizar = estadoNuevo.text.toString()

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

    //----------------------------------------------------------------------------------------------

    override fun onItemSelected(adapterView: AdapterView<*>, view: View?, i: Int, l: Long) {

        val estado_seleccionado = adapterView.getItemAtPosition(i).toString()
        estadoNuevo.text = estado_seleccionado

        if (estado_seleccionado == "pendiente") {
            tareaNoFinalizada.visibility = View.VISIBLE
            tareaFinalizada.visibility = View.GONE
        }else {
            tareaFinalizada.visibility = View.VISIBLE
            tareaNoFinalizada.visibility = View.GONE
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_actualizar, menu)
        this.menu = menu
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.Actualizar_Nota_BD -> ActualizarNotaDB()
            R.id.ExtraerTexto -> {

                menu?.findItem(R.id.Actualizar_Nota_BD)?.isVisible = true
                menu?.findItem(R.id.ExtraerTexto)?.isVisible = false
                menu?.findItem(R.id.LimpiarDibujo)?.isVisible = false

                val layoutParams = scrollNota.layoutParams
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                scrollNota.layoutParams = layoutParams

                descripcionA.visibility = View.VISIBLE
                linearDraw.visibility = View.GONE

                StrokeManager.recognize(
                    descripcionA
                )

                drawView.clear()
                StrokeManager.clear()

                return true
            }
            R.id.LimpiarDibujo -> {
                drawView.clear()
                StrokeManager.clear()
                descripcionA.setText("")
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
                    fechaA.text = "$diaFormateado/$mesFormateado/$anioSeleccionado"
                }, anio, mes, dia)

                datePickerDialog.show()

            }
            R.id.listado -> {

                Toast.makeText(this, "Listado", Toast.LENGTH_SHORT).show()
            }
            R.id.inputs -> {

                menu?.findItem(R.id.Actualizar_Nota_BD)?.isVisible = true
                menu?.findItem(R.id.ExtraerTexto)?.isVisible = false
                menu?.findItem(R.id.LimpiarDibujo)?.isVisible = false

                val layoutParams = scrollNota.layoutParams
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                scrollNota.layoutParams = layoutParams

                descripcionA.visibility = View.VISIBLE
                linearDraw.visibility = View.GONE

            }
            R.id.painter -> {

                menu?.findItem(R.id.Actualizar_Nota_BD)?.isVisible = false
                menu?.findItem(R.id.ExtraerTexto)?.isVisible = true
                menu?.findItem(R.id.LimpiarDibujo)?.isVisible = true

                val layoutParams = scrollNota.layoutParams
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                scrollNota.layoutParams = layoutParams

                descripcionA.visibility = View.GONE
                linearDraw.visibility = View.VISIBLE
            }
            R.id.extractor -> {

                Toast.makeText(this, "Extractor texto", Toast.LENGTH_SHORT).show()
            }
        }
            false
    }

}