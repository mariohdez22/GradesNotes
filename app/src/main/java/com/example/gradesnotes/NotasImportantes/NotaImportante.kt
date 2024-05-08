package com.example.gradesnotes.NotasImportantes

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gradesnotes.Modelos.Nota
import com.example.gradesnotes.R
import com.example.gradesnotes.ViewHolder.ViewHolderNotaImportante
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class NotaImportante : AppCompatActivity() {

    private lateinit var recyclerViewNotasImportantes: RecyclerView
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var misUsuariosRef: DatabaseReference

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var user: FirebaseUser

    private lateinit var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<Nota, ViewHolderNotaImportante>
    private lateinit var firebaseRecyclerOptions: FirebaseRecyclerOptions<Nota>

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nota_importante)

        val actionBar = supportActionBar
        actionBar?.title = "Notas importantes"
        actionBar?.setDisplayShowHomeEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerViewNotasImportantes = findViewById(R.id.RecyclerViewNotasImportantes)
        recyclerViewNotasImportantes.setHasFixedSize(true)

        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        user = firebaseAuth.currentUser ?: throw IllegalStateException("Usuario no encontrado")

        misUsuariosRef = firebaseDatabase.getReference("Usuarios")

        dialog = Dialog(this)

        comprobarUsuario()

    }

    private fun comprobarUsuario() {
        if (user == null) {
            Toast.makeText(this@NotaImportante, "Ha ocurrido un error", Toast.LENGTH_SHORT)
                .show()
        } else {
            listarNotasImportantes()
        }
    }

    private fun listarNotasImportantes() {

        val query = misUsuariosRef.child(user.uid).child("MisNotasImportantes").orderByChild("fechaNota")

        firebaseRecyclerOptions = FirebaseRecyclerOptions.Builder<Nota>()
            .setQuery(query, Nota::class.java)
            .build()

        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Nota, ViewHolderNotaImportante>(firebaseRecyclerOptions) {
            override fun onBindViewHolder(holder: ViewHolderNotaImportante, position: Int, model: Nota) {
                holder.setearDatos(
                    applicationContext,
                    model.idNota,
                    model.uidUsuario,
                    model.correoUsuario,
                    model.fechaHoraActual,
                    model.titulo,
                    model.descripcion,
                    model.fechaNota,
                    model.estado
                )
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderNotaImportante {

                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_nota_importante, parent, false)
                return ViewHolderNotaImportante(view).apply {
                    setOnClickListener(object : ViewHolderNotaImportante.ClickListener {

                        override fun onItemClick(view: View, position: Int) {


                        }

                        override fun onItemLongClick(view: View, position: Int) {
                            val idNota = getItem(position).idNota
                            mostrarDialogoEliminarNota(idNota)
                        }
                    })
                }
            }
        }

        linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false).apply {
            reverseLayout = true
            stackFromEnd = true
        }

        recyclerViewNotasImportantes.layoutManager = linearLayoutManager
        recyclerViewNotasImportantes.adapter = firebaseRecyclerAdapter
    }

    private fun mostrarDialogoEliminarNota(idNota: String) {

        dialog.setContentView(R.layout.cuadro_dialogo_eliminar_nota_importante)
        val btnEliminarNota = dialog.findViewById<Button>(R.id.EliminarNota)
        val btnCancelar = dialog.findViewById<Button>(R.id.EliminarNotaCancelar)

        btnEliminarNota.setOnClickListener {
            eliminarNotaImportante(idNota)
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener {
            Toast.makeText(this, "Cancelado por el usuario", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun eliminarNotaImportante(idNota: String) {
        if (user == null) {
            Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
        } else {
            val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
            reference.child(user.uid).child("MisNotasImportantes").child(idNota)
                .removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "La nota ya no es importante", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onStart() {
        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.startListening()
        }
        super.onStart()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

}