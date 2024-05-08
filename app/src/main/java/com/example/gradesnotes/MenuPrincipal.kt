package com.example.gradesnotes

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.bumptech.glide.Glide
import com.example.gradesnotes.AgregarNotas.AgregarNota
import com.example.gradesnotes.ListarNotas.ListarNota
import com.example.gradesnotes.NotasImportantes.NotaImportante
import com.example.gradesnotes.Perfiles.PerfilUsuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MenuPrincipal : AppCompatActivity() {

    private lateinit var CardAgregar: CardView
    private lateinit var CardNotas: CardView
    private lateinit var CardArchivados: CardView
    private lateinit var CardPerfil: CardView
    private lateinit var CardAcercaDe: CardView
    private lateinit var CardSalir: CardView
    private lateinit var VerificacionCuenta: TextView

    private lateinit var LinearVerificacion: LinearLayout
    private lateinit var LinearUsuario: LinearLayout
    private lateinit var LinearCorreo: LinearLayout

    private lateinit var progressBarDialog: AlertDialog
    private lateinit var Usuarios: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var user: FirebaseUser

    private lateinit var UidPrincipal: TextView
    private lateinit var NombresPrincipal: TextView
    private lateinit var CorreoPrincipal: TextView
    private lateinit var cardImgPerfil: CardView
    private lateinit var imagenPerfil: ImageView
    private lateinit var iconVerify: ImageView
    private lateinit var progressBarDatos: ProgressBar

    private lateinit var dialog_cuenta_verificada: Dialog
    private lateinit var dialog_informacion: Dialog

    //----------------------------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_principal)

        //val toolbar = findViewById<Toolbar>(R.id.PerfilToolbar)
        //setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        actionBar?.title = "Skript"

        CardAgregar = findViewById(R.id.CardAgregar)
        CardNotas = findViewById(R.id.CardNotas)
        CardArchivados = findViewById(R.id.CardArchivados)
        CardPerfil = findViewById(R.id.CardPerfil)
        CardAcercaDe = findViewById(R.id.CardAcercaDe)
        CardSalir = findViewById(R.id.CardSalir)
        VerificacionCuenta = findViewById(R.id.VerificacionCuenta)

        LinearVerificacion = findViewById(R.id.Linear_Verficacion)
        LinearUsuario = findViewById(R.id.linearNombre)
        LinearCorreo = findViewById(R.id.linearCorreo)

        UidPrincipal = findViewById(R.id.UidPrincipal)
        NombresPrincipal = findViewById(R.id.NombresPrincipal)
        CorreoPrincipal = findViewById(R.id.CorreoPrincipal)
        cardImgPerfil = findViewById(R.id.cardImgPerfil)
        imagenPerfil = findViewById(R.id.imgPerfil)
        iconVerify = findViewById(R.id.iconVerify)
        progressBarDatos = findViewById(R.id.progressBarDatos)

        val progressBar = ProgressBar(this)
        progressBar.isIndeterminate = true

        val builder = android.app.AlertDialog.Builder(this)
        builder.setView(progressBar)
        builder.setTitle("Espere por favor")
        builder.setCancelable(false) // para hacerlo no cancelable

        progressBarDialog = builder.create()

        Usuarios = FirebaseDatabase.getInstance().getReference("Usuarios")
        firebaseAuth = FirebaseAuth.getInstance()
        user = firebaseAuth.currentUser!!

        dialog_informacion = Dialog(this, R.style.TransparentDialog)
        dialog_cuenta_verificada = Dialog(this)

        VerificacionCuenta.setOnClickListener(View.OnClickListener {
            if (user.isEmailVerified) {

                AnimacionCuentaVerificada()
            } else {
                //Si la cuenta no está verificada
                VerificarCuentaCorreo()
            }
        })

        CardAgregar.setOnClickListener {

            // Obtenemos la información de los TextView
            val uidUsuario = UidPrincipal.text.toString()
            val correoUsuario = CorreoPrincipal.text.toString()

            // Pasamos datos a la siguiente actividad
            val intent = Intent(this, AgregarNota::class.java).apply {
                putExtra("Uid", uidUsuario)
                putExtra("Correo", correoUsuario)
            }
            startActivity(intent)
        }

        cardImgPerfil.setOnClickListener{
            startActivity(Intent(this@MenuPrincipal, PerfilUsuario::class.java))
            Toast.makeText(this, "Perfil Usuario", Toast.LENGTH_SHORT).show()
        }

        CardNotas.setOnClickListener {
            startActivity(Intent(this, ListarNota::class.java))
            Toast.makeText(this, "Lista de Notas", Toast.LENGTH_SHORT).show()
        }


        CardArchivados.setOnClickListener {
            startActivity(Intent(this, NotaImportante::class.java))
            Toast.makeText(this, "Notas Importantes", Toast.LENGTH_SHORT).show()
        }

        CardPerfil.setOnClickListener {
            startActivity(Intent(this, PerfilUsuario::class.java))
            Toast.makeText(this, "Perfil Usuario", Toast.LENGTH_SHORT).show()
        }

        CardAcercaDe.setOnClickListener {
            Informacion()
        }

        CardSalir.setOnClickListener {
            salirAplicacion()
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun VerificarCuentaCorreo() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("verificar cuenta")
            .setMessage(
                "¿Estás seguro(a) de enviar instrucciones de verificación a su correo electrónico? " + user.email
            )
            .setPositiveButton(
                "Enviar"
            ) { dialogInterface, i -> EnviarCorreoAVerificacion() }
            .setNegativeButton(
                "Cancelar"
            ) { dialogInterface, i ->
                Toast.makeText(
                    this@MenuPrincipal,
                    "Cancelado por el usuario",
                    Toast.LENGTH_SHORT
                ).show()
            }.show()
    }

    //----------------------------------------------------------------------------------------------

    private fun EnviarCorreoAVerificacion()
    {
        progressBarDialog.setMessage("Enviando instrucciones de verificación a su correo electrónico " + user.email);
        showProgressBarDialog()

        user.sendEmailVerification()
            .addOnSuccessListener { //Envío fue exitoso
                dismissProgressBarDialog()
                Toast.makeText(
                    this@MenuPrincipal,
                    "Instrucciones enviadas, revise su bandeja " + user.email,
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e -> //Falló el envío
                Toast.makeText(
                    this@MenuPrincipal,
                    "Falló debido a: " + e.message,
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
    }

    //----------------------------------------------------------------------------------------------

    private fun VerificarEstadoDeCuenta() {

        val Verificado = "Verificado"
        val No_Verificado = "No Verificado"
        if (user.isEmailVerified) {

            VerificacionCuenta.text = Verificado
            VerificacionCuenta.setTextColor(Color.rgb(255, 229, 85 ))

            var drawable = ContextCompat.getDrawable(this, R.drawable.icon_verificacion)
            drawable = drawable?.let { DrawableCompat.wrap(it) };
            if (drawable != null) {
                DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.Verify))
            };

            iconVerify.setImageDrawable(drawable)

        } else {

            VerificacionCuenta.text = No_Verificado
            VerificacionCuenta.setTextColor(Color.rgb(241, 154, 255 ))

            var drawable = ContextCompat.getDrawable(this, R.drawable.icon_verificacion)
            drawable = drawable?.let { DrawableCompat.wrap(it) };
            if (drawable != null) {
                DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.NotVerify))
            };

            iconVerify.setImageDrawable(drawable)
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun AnimacionCuentaVerificada() {

        dialog_cuenta_verificada.setContentView(R.layout.dialogo_cuenta_verificada)

        val EntendidoVerificado: Button = dialog_cuenta_verificada.findViewById<Button>(R.id.EntendidoVerificado)

        EntendidoVerificado.setOnClickListener {

            dialog_cuenta_verificada.dismiss()
        }

        dialog_cuenta_verificada.show()
        dialog_cuenta_verificada.setCanceledOnTouchOutside(false)
    }

    //----------------------------------------------------------------------------------------------

    private fun Informacion() {

        dialog_informacion.setContentView(R.layout.cuadro_dialogo_informacion)

        val EntendidoInfo: Button = dialog_informacion.findViewById<Button>(R.id.EntendidoInfo)

        EntendidoInfo.setOnClickListener {

            dialog_informacion.dismiss()
        }

        dialog_informacion.show()
        dialog_informacion.setCanceledOnTouchOutside(false)
    }

    //----------------------------------------------------------------------------------------------

    override fun onStart() {

        verificarInicioSesion()
        super.onStart()
    }

    //----------------------------------------------------------------------------------------------

    private fun verificarInicioSesion() {

        if (user != null) {

            cargaDeDatos()

        }else {

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun cargaDeDatos(){

        VerificarEstadoDeCuenta()

        Usuarios.child(user.uid).addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {

                    progressBarDatos.visibility = View.GONE
                    cardImgPerfil.visibility = View.VISIBLE
                    LinearUsuario.visibility = View.VISIBLE
                    LinearCorreo.visibility = View.VISIBLE
                    LinearVerificacion.visibility = View.VISIBLE

                    val uid = snapshot.child("uid").getValue(String::class.java) ?: ""
                    val nombres = snapshot.child("nombres").getValue(String::class.java) ?: ""
                    val correo = snapshot.child("correo").getValue(String::class.java) ?: ""
                    val imgPerfil = snapshot.child("imagen_perfil").getValue(String::class.java) ?: ""

                    UidPrincipal.text = uid
                    NombresPrincipal.text = nombres
                    CorreoPrincipal.text = correo

                    cargarImagen(imgPerfil)

                    //AgregarNotas.isEnabled = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar error si es necesario
            }

        })
    }

    //----------------------------------------------------------------------------------------------

    private fun cargarImagen(imagenPerfils: String?) {
        try {
            // Cuando la imagen ha sido traída exitosamente desde Firebase
            Glide.with(applicationContext)
                .load(imagenPerfils)
                .placeholder(R.drawable.icon_perfil)
                .into(imagenPerfil)
        } catch (e: Exception) {
            // Si la imagen no fue traída con éxito
            Glide.with(applicationContext)
                .load(R.drawable.icon_perfil)
                .into(imagenPerfil)
        }
    }

    //----------------------------------------------------------------------------------------------

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_principal, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //----------------------------------------------------------------------------------------------

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.Perfil_usuario) {
            startActivity(Intent(this@MenuPrincipal, PerfilUsuario::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    //----------------------------------------------------------------------------------------------

    private fun salirAplicacion() {

        firebaseAuth.signOut()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        Toast.makeText(this, "Sesion Cerrada", Toast.LENGTH_SHORT).show()
    }

    //----------------------------------------------------------------------------------------------

    private fun showProgressBarDialog() {
        progressBarDialog.show()
    }

    //----------------------------------------------------------------------------------------------

    private fun dismissProgressBarDialog() {
        if (progressBarDialog.isShowing) {
            progressBarDialog.dismiss()
        }
    }

    //----------------------------------------------------------------------------------------------
}