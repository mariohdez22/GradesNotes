package com.example.gradesnotes.AgregarNotas

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.transition.Visibility
import com.example.gradesnotes.DrawNota.DrawView
import com.example.gradesnotes.DrawNota.StrokeManager.clear
import com.example.gradesnotes.DrawNota.StrokeManager.download
import com.example.gradesnotes.DrawNota.StrokeManager.recognize
import com.example.gradesnotes.Modelos.Nota
import com.example.gradesnotes.R
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.mlkit.vision.common.InputImage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

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
    private var uri: Uri? = null
    private lateinit var dialog : Dialog
    lateinit var textRecognizer : TextRecognizer

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

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
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
                menu?.findItem(R.id.ExtraerTexto)?.isVisible = false
                menu?.findItem(R.id.LimpiarDibujo)?.isVisible = false

                val layoutParams = scrollNota.layoutParams
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                scrollNota.layoutParams = layoutParams

                descripcion.visibility = View.GONE
                linearDraw.visibility = View.VISIBLE
            }
            R.id.extractor -> {

                dialog = Dialog(this)
                dialog.setContentView(R.layout.cuadro_dialogo_elegir_imagen)

                dialog.show()

                var btnElegirGaleria = dialog.findViewById<Button>(R.id.Btn_Elegir_Galeria)
                var btnElegirCamara = dialog.findViewById<Button>(R.id.Btn_Elegir_Camara)

                btnElegirCamara.setOnClickListener {
                    abrirCamara()

                    dialog.dismiss()
                }

                btnElegirGaleria.setOnClickListener {
                    abrirGaleria()

                    dialog.dismiss()
                }

            }
        }
        false
    }

    private fun reconocerTexto(){

        uri = Uri.parse(descripcion.text.toString())
        descripcion.setText("")

        if(uri != null){


        }

    }

    private fun abrirGaleria(){

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED){
            var intent = Intent(Intent.ACTION_PICK)
            intent.setType("image/*")
            galeriaARL.launch(intent)
        }
        else{
            permisoGaleriaARL.launch(Manifest.permission.READ_MEDIA_IMAGES)
        }
    }

    private fun abrirCamara(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            var values = ContentValues()

            values.put(MediaStore.Images.Media.TITLE, "Titulo")
            values.put(MediaStore.Images.Media.DESCRIPTION, "Descripcion")

            uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

            camaraARL.launch(intent)
        }else{
            permisoCamaraARL.launch(Manifest.permission.CAMERA)
        }

        reconocerTexto()
    }

    private var galeriaARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
            result : ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK){
            var data = result.data
            if (data != null) {

                uri = data.data

                var inputImage = InputImage.fromFilePath(this, uri!!)

                var textTask = textRecognizer.process(inputImage)
                    .addOnSuccessListener {
                            text : Text ->
                        var texto = text.text
                        descripcion.setText(texto)
                    }
                    .addOnFailureListener {
                            e : Exception ->
                        Toast.makeText(this, "Error -> " + e.message, Toast.LENGTH_SHORT).show()
                    }
                Toast.makeText(this, "FOTO SELECCIONADA CON EXITO", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this, "CANCELADO POR EL USUARIO", Toast.LENGTH_SHORT).show()
        }
    }

    private var camaraARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
            result : ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK){
            if(uri != null){
                var inputImage = InputImage.fromFilePath(this, uri!!)

                var textTask = textRecognizer.process(inputImage)
                    .addOnSuccessListener {
                            text : Text ->
                        var texto = text.text
                        descripcion.setText(texto)
                    }
                    .addOnFailureListener {
                            e : Exception ->
                        Toast.makeText(this, "Error -> " + e.message, Toast.LENGTH_SHORT).show()
                    }
                Toast.makeText(this, "FOTO TOMADA CON EXITO", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this, "CANCELADO POR EL USUARIO", Toast.LENGTH_SHORT).show()
        }
    }

    private var permisoCamaraARL = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            abrirCamara()
        } else {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private var permisoGaleriaARL = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            abrirGaleria()
        } else {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }

}