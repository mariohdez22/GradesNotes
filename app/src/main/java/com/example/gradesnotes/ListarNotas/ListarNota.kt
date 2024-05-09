package com.example.gradesnotes.ListarNotas

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gradesnotes.ActualizarNotas.ActualizarNota
import com.example.gradesnotes.AgregarNotas.AgregarNota
import com.example.gradesnotes.Detalle.DetalleNota
import com.example.gradesnotes.Modelos.Nota
import com.example.gradesnotes.R
import com.example.gradesnotes.ViewHolder.ViewHolder_Nota
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ListarNota : AppCompatActivity() {

    private var idNotaActual: String? = null
    private lateinit var recyclerViewNotas: RecyclerView
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var baseDeDatos: DatabaseReference
    private lateinit var dialog: Dialog
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<Nota, ViewHolder_Nota>
    private lateinit var options: FirebaseRecyclerOptions<Nota>

    private var comprobarNotaImportante = false
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var user: FirebaseUser

    private lateinit var bottomSheetView: View
    private lateinit var bottomSheetDialog: BottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_nota)

        val toolbar = findViewById<Toolbar>(R.id.ListaToolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "Mis notas"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        recyclerViewNotas = findViewById(R.id.recyclerviewNotas)
        recyclerViewNotas.setHasFixedSize(true)

        firebaseAuth = FirebaseAuth.getInstance()
        user = firebaseAuth.currentUser!!

        firebaseDatabase = FirebaseDatabase.getInstance()
        baseDeDatos = firebaseDatabase.getReference("NotasPublicadas")
        dialog = Dialog(this)
        listarNotasUsuarios()
        setupBottomSheet()
    }

    private fun setupBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetView = layoutInflater.inflate(R.layout.bottomsheet, null)
        bottomSheetDialog.setContentView(bottomSheetView)
    }

    private fun AgregarIdNota(idNota: String) {

        idNotaActual = idNota

    }

    private fun verificarNotaImportante(idNota: String? = null) {

        if (idNota != null) {

            val referencia = FirebaseDatabase.getInstance().getReference("Usuarios")
            referencia.child(firebaseAuth.uid!!).child("MisNotasImportantes").child(idNota ?: "")
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {

                        comprobarNotaImportante = snapshot.exists()
                        actualizarBotonImportante()
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private fun actualizarBotonImportante() {

        val iconFavoritos = bottomSheetView.findViewById<ImageView>(R.id.imagenFavoritos)
        val txtFavoritos = bottomSheetView.findViewById<TextView>(R.id.textoFavoritos)

        if (comprobarNotaImportante) {
            txtFavoritos.text = "Eliminar de Favoritos"
            iconFavoritos.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icono_nota_no_importante))
        } else {
            txtFavoritos.text = "Añadir a Favoritos"
            iconFavoritos.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icono_nota_importante))
        }
    }

    private fun listarNotasUsuarios() {

        val query = baseDeDatos.orderByChild("uidUsuario").equalTo(user.uid)

        options = FirebaseRecyclerOptions.Builder<Nota>()
            .setQuery(query, Nota::class.java)
            .build()

        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Nota, ViewHolder_Nota>(options) {
            override fun onBindViewHolder(holder: ViewHolder_Nota, position: Int, model: Nota) {
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

                verificarNotaImportante(model.idNota)

            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder_Nota {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_nota, parent, false)
                return ViewHolder_Nota(view).apply {
                    setOnClickListener(object : ViewHolder_Nota.ClickListener {

                        override fun onItemClick(view: View, position: Int) {

                            val nota = getItem(position)
                            // Obtener los datos de la nota seleccionada
                            val idNota = nota.idNota
                            val uidUsuario = nota.uidUsuario
                            val correoUsuario = nota.correoUsuario
                            val fechaRegistro = nota.fechaHoraActual
                            val titulo = nota.titulo
                            val descripcion = nota.descripcion
                            val fechaNota = nota.fechaNota
                            val estado = nota.estado

                            val intent = Intent(this@ListarNota, ActualizarNota::class.java).apply {
                                putExtra("id_nota", idNota)
                                putExtra("uid_usuario", uidUsuario)
                                putExtra("correo_usuario", correoUsuario)
                                putExtra("fecha_registro", fechaRegistro)
                                putExtra("titulo", titulo)
                                putExtra("descripcion", descripcion)
                                putExtra("fecha_nota", fechaNota)
                                putExtra("estado", estado)
                            }
                            startActivity(intent)

                        }

                        override fun onItemLongClick(view: View, position: Int) {

                            val nota = getItem(position)
                            // Obtener los datos de la nota seleccionada
                            val idNota = nota.idNota
                            val uidUsuario = nota.uidUsuario
                            val correoUsuario = nota.correoUsuario
                            val fechaRegistro = nota.fechaHoraActual
                            val titulo = nota.titulo
                            val descripcion = nota.descripcion
                            val fechaNota = nota.fechaNota
                            val estado = nota.estado

                            AgregarIdNota(idNota)

                            if (idNotaActual != null) {
                                verificarNotaImportante(idNotaActual)
                            }

                            showBottomSheet(view, idNota, uidUsuario, correoUsuario, fechaRegistro, titulo, descripcion, fechaNota, estado)

                        }
                    })
                }
            }
        }

        linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false).apply {
            reverseLayout = true
            stackFromEnd = true
        }

        recyclerViewNotas.layoutManager = linearLayoutManager
        recyclerViewNotas.adapter = firebaseRecyclerAdapter
    }

    private fun eliminarNota(idNota: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Eliminar nota")
            .setMessage("¿Desea eliminar la nota?")
            .setPositiveButton("Sí") { _, _ ->
                baseDeDatos.orderByChild("idNota").equalTo(idNota)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (ds in snapshot.children) {
                                ds.ref.removeValue()
                            }
                            //Toast.makeText(this@ListarNota, "Nota eliminada", Toast.LENGTH_SHORT).show()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@ListarNota, "Error al eliminar nota: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNegativeButton("No") { _, _ ->
                Toast.makeText(this, "Cancelado por el usuario", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun vaciarRegistroDeNotas() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Vaciar todos los registros")
        builder.setMessage("¿Estás seguro(a) de eliminar todas las notas?")

        builder.setPositiveButton("Sí") { dialog, which ->

            val query = baseDeDatos.orderByChild("uidUsuario").equalTo(user.uid)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        ds.ref.removeValue()
                    }
                    Toast.makeText(this@ListarNota, "Todas las notas se han eliminado correctamente", Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar posibles errores
                }
            })
        }

        builder.setNegativeButton("No") { dialog, which ->
            Toast.makeText(this@ListarNota, "Cancelado por el usuario", Toast.LENGTH_SHORT).show()
        }

        builder.create().show()
    }

    fun showBottomSheet(view: View,
                        idNota: String,
                        uidUsuario: String,
                        correoUsuario: String,
                        fechaRegistro: String,
                        titulo: String,
                        descripcion: String,
                        fechaNota: String,
                        estado: String)
    {

        val btnDetalles = bottomSheetView .findViewById<LinearLayout>(R.id.linearDetalles)
        val btnEliminarNota = bottomSheetView .findViewById<LinearLayout>(R.id.linearEliminar)
        val btnFavoritos = bottomSheetView .findViewById<LinearLayout>(R.id.linearFavoritos)

        btnFavoritos.setOnClickListener {

            if (comprobarNotaImportante) {

                val referencia = FirebaseDatabase.getInstance().getReference("Usuarios")
                referencia.child(user.uid).child("MisNotasImportantes").child(idNota ?: "")
                    .removeValue()
                    .addOnSuccessListener {

                        //Toast.makeText(this, "La nota ya no es importante", Toast.LENGTH_SHORT).show()

                    }.addOnFailureListener {

                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    }

            } else {

                val notaImportante = hashMapOf(
                    "idNota" to idNota,
                    "uidUsuario" to uidUsuario,
                    "correoUsuario" to correoUsuario,
                    "fechaHoraActual" to fechaRegistro,
                    "titulo" to titulo,
                    "descripcion" to descripcion,
                    "fechaNota" to fechaNota,
                    "estado" to estado,
                )

                val referencia = FirebaseDatabase.getInstance().getReference("Usuarios")
                referencia.child(firebaseAuth.uid!!).child("MisNotasImportantes").child(idNota ?: "")
                    .setValue(notaImportante)
                    .addOnSuccessListener {

                        //Toast.makeText(this, "Se ha añadido a notas importantes", Toast.LENGTH_SHORT).show()

                    }.addOnFailureListener {

                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    }
            }

        }

        btnEliminarNota.setOnClickListener {
            eliminarNota(idNota)
            bottomSheetDialog.dismiss()
        }

        btnDetalles.setOnClickListener {

            val intent = Intent(this@ListarNota, DetalleNota::class.java).apply {
                putExtra("id_nota", idNota)
                putExtra("uid_usuario", uidUsuario)
                putExtra("correo_usuario", correoUsuario)
                putExtra("fecha_registro", fechaRegistro)
                putExtra("titulo", titulo)
                putExtra("descripcion", descripcion)
                putExtra("fecha_nota", fechaNota)
                putExtra("estado", estado)
            }
            startActivity(intent)

            bottomSheetDialog.dismiss()
        }

        //dialog.setCancelable(false)
        bottomSheetDialog.show()
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_vaciar_todas_las_notas, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.Vaciar_Todas_Las_Notas) {
            vaciarRegistroDeNotas()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        if (user != null) {
            firebaseRecyclerAdapter.startListening()
        }
        super.onStart()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }
}




















/*

val intent = Intent(this@ListarNota, DetalleNota::class.java).apply {
    putExtra("id_nota", idNota)
    putExtra("uid_usuario", uidUsuario)
    putExtra("correo_usuario", correoUsuario)
    putExtra("fecha_registro", fechaRegistro)
    putExtra("titulo", titulo)
    putExtra("descripcion", descripcion)
    putExtra("fecha_nota", fechaNota)
    putExtra("estado", estado)
}
startActivity(intent)

*/

/*

// Configurar y mostrar el diálogo de opciones
dialog.setContentView(R.layout.dialogo_opciones)

val btnEliminar: Button = dialog.findViewById(R.id.CD_Eliminar)
val btnActualizar: Button = dialog.findViewById(R.id.CD_Actualizar)

btnEliminar.setOnClickListener {
    eliminarNota(idNota)
    dialog.dismiss()
}

btnActualizar.setOnClickListener {
    val intent = Intent(this@ListarNota, ActualizarNota::class.java).apply {
        putExtra("id_nota", idNota)
        putExtra("uid_usuario", uidUsuario)
        putExtra("correo_usuario", correoUsuario)
        putExtra("fecha_registro", fechaRegistro)
        putExtra("titulo", titulo)
        putExtra("descripcion", descripcion)
        putExtra("fecha_nota", fechaNota)
        putExtra("estado", estado)
    }
    startActivity(intent)
    dialog.dismiss()
}

dialog.show()

*/