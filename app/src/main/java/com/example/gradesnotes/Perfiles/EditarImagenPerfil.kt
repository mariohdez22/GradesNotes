package com.example.gradesnotes.Perfiles

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.gradesnotes.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class EditarImagenPerfil : AppCompatActivity() {

    private lateinit var imagenPerfilActualizar: ImageView
    private lateinit var btnElegirImagenDe: Button
    private lateinit var btnActualizarImagen: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var user: FirebaseUser

    private lateinit var dialogElegirImagen: Dialog
    private lateinit var imagenUri: Uri

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_imagen_perfil)

        val actionBar = supportActionBar
        actionBar?.title = "Seleccionar imagen"
        actionBar?.setDisplayShowHomeEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        imagenPerfilActualizar = findViewById(R.id.ImagenPerfilActualizar)
        btnElegirImagenDe = findViewById(R.id.BtnElegirImagenDe)
        btnActualizarImagen = findViewById(R.id.BtnActualizarImagen)

        firebaseAuth = FirebaseAuth.getInstance()
        user = firebaseAuth.currentUser!!

        dialogElegirImagen = Dialog(this)

        btnElegirImagenDe.setOnClickListener {
            elegirImagenDe()
        }

        btnActualizarImagen.setOnClickListener {

            if (imagenUri == null) {

                Toast.makeText(this@EditarImagenPerfil, "Inserte una nueva imagen", Toast.LENGTH_SHORT).show()

            } else {
                subirImagenStorage()
            }
        }

        progressDialog = ProgressDialog(this@EditarImagenPerfil)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        lecturaDeImagen()
    }

    //----------------------------------------------------------------------------------------------

    //esto si sirve
    private fun lecturaDeImagen() {
        val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
        reference.child(user.uid ?: "").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Obtener el dato imagen
                val imagenPerfil = snapshot.child("imagen_perfil").getValue(String::class.java) ?: ""
                Glide.with(applicationContext)
                    .load(imagenPerfil)
                    .placeholder(R.drawable.imagen_perfil_usuario)
                    .into(imagenPerfilActualizar)
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar error de cancelación
            }
        })
    }

    //----------------------------------------------------------------------------------------------

    private fun subirImagenStorage() {

        val carpetaImagenes = "ImagenesPerfil/"
        val NombreImagen = carpetaImagenes + firebaseAuth.uid
        val reference = FirebaseStorage.getInstance().getReference(NombreImagen)
        reference.putFile(imagenUri)
            .addOnSuccessListener { taskSnapshot ->

                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uriImagen = "" + uriTask.result
                ActualizarImagenBD(uriImagen)

            }.addOnFailureListener { e ->
                Toast.makeText(
                    this@EditarImagenPerfil,
                    "" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun ActualizarImagenBD(uriImagen: String) {

        val hashMap = HashMap<String, Any>()
        hashMap["imagen_perfil"] = "" + uriImagen

        val databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios")
        databaseReference.child(user.uid)
            .updateChildren(hashMap)
            .addOnSuccessListener {

                Toast.makeText(this@EditarImagenPerfil, "Imagen se ha actualizado con éxito", Toast.LENGTH_SHORT).show()
                onBackPressedDispatcher.onBackPressed()

            }.addOnFailureListener { e ->

                Toast.makeText(this@EditarImagenPerfil, "" + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    //----------------------------------------------------------------------------------------------

    private fun elegirImagenDe() {

        dialogElegirImagen.setContentView(R.layout.cuadro_dialogo_elegir_imagen)

        val btnElegirGaleria: Button = dialogElegirImagen.findViewById(R.id.Btn_Elegir_Galeria)
        val btnElegirCamara: Button = dialogElegirImagen.findViewById(R.id.Btn_Elegir_Camara)

        btnElegirGaleria.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                seleccionarImagenGaleria()
                dialogElegirImagen.dismiss()

            } else {
                solicitudPermisoGaleria.launch(Manifest.permission.READ_MEDIA_IMAGES)
                dialogElegirImagen.dismiss()
            }
        }

        btnElegirCamara.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                seleccionarImagenCamara()
                dialogElegirImagen.dismiss()

            } else {
                solicitudPermisoCamara.launch(Manifest.permission.CAMERA)
                dialogElegirImagen.dismiss()
            }
        }

        dialogElegirImagen.show()
        dialogElegirImagen.setCanceledOnTouchOutside(true)
    }

    //----------------------------------------------------------------------------------------------

    private fun seleccionarImagenGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galeriaActivityResultLauncher.launch(intent)
    }

    private val galeriaActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Obtener URI de la imagen
            imagenUri = result.data?.data!!
            // Setear la imagen seleccionada en el imageView
            imagenPerfilActualizar.setImageURI(imagenUri)
        } else {
            Toast.makeText(this, "Cancelado por el usuario", Toast.LENGTH_SHORT).show()
        }
    }

    /* PERMISO PARA ACCEDER A LA GALERIA */
    private val solicitudPermisoGaleria = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            seleccionarImagenGaleria()
        } else {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun seleccionarImagenCamara() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "Nueva imagen")
            put(MediaStore.Images.Media.DESCRIPTION, "Descripción de imagen")
        }
        imagenUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imagenUri)
        }
        camaraActivityResultLauncher.launch(intent)
    }

    private val camaraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imagenPerfilActualizar.setImageURI(imagenUri)
        } else {
            Toast.makeText(this@EditarImagenPerfil, "Cancelado por el usuario", Toast.LENGTH_SHORT).show()
        }
    }

    private val solicitudPermisoCamara = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            seleccionarImagenCamara()
        } else {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }

    //----------------------------------------------------------------------------------------------

}