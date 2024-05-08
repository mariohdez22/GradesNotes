package com.example.gradesnotes.ViewHolder

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gradesnotes.R

class ViewHolderNotaImportante(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var mView: View = itemView
    private var mClickListener: ClickListener? = null

    interface ClickListener {
        fun onItemClick(view: View, position: Int)
        fun onItemLongClick(view: View, position: Int)
    }

    fun setOnClickListener(listener: ClickListener) {
        mClickListener = listener
    }

    init {
        itemView.setOnClickListener {
            mClickListener?.onItemClick(it, bindingAdapterPosition)
        }

        itemView.setOnLongClickListener {
            mClickListener?.onItemLongClick(it, bindingAdapterPosition)
            false
        }
    }

    fun setearDatos(
        context: Context,
        idNota: String,
        uidUsuario: String,
        correoUsuario: String,
        fechaHoraRegistro: String,
        titulo: String,
        descripcion: String,
        fechaNota: String,
        estado: String
    ) {

        val idNotaItem: TextView = mView.findViewById(R.id.Id_nota_Item_I)
        val uidUsuarioItem: TextView = mView.findViewById(R.id.Uid_Usuario_Item_I)
        val correoUsuarioItem: TextView = mView.findViewById(R.id.Correo_usuario_Item_I)
        val fechaHoraRegistroItem: TextView = mView.findViewById(R.id.Fecha_hora_registro_Item_I)
        val tituloItem: TextView = mView.findViewById(R.id.Titulo_Item_I)
        val descripcionItem: TextView = mView.findViewById(R.id.Descripcion_Item_I)
        val fechaItem: TextView = mView.findViewById(R.id.Fecha_Item_I)
        val estadoItem: TextView = mView.findViewById(R.id.Estado_Item_I)
        val tareaFinalizadaItem: ImageView = mView.findViewById(R.id.Tarea_Finalizada_Item_I)
        val tareaNoFinalizadaItem: ImageView = mView.findViewById(R.id.Tarea_No_Finalizada_Item_I)

        idNotaItem.text = idNota
        uidUsuarioItem.text = uidUsuario
        correoUsuarioItem.text = correoUsuario
        fechaHoraRegistroItem.text = fechaHoraRegistro
        tituloItem.text = titulo
        descripcionItem.text = descripcion
        fechaItem.text = fechaNota
        estadoItem.text = estado

        if (estado == "Finalizado") {
            tareaFinalizadaItem.visibility = View.VISIBLE
            tareaNoFinalizadaItem.visibility = View.GONE
        } else {
            tareaNoFinalizadaItem.visibility = View.VISIBLE
            tareaFinalizadaItem.visibility = View.GONE
        }
    }

}