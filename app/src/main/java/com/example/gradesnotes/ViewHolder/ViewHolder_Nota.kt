package com.example.gradesnotes.ViewHolder

import android.content.Context
import android.view.View
import android.widget.Adapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.gradesnotes.R

class ViewHolder_Nota(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var mView: View = itemView
    private var mClickListener: ClickListener? = null

    interface ClickListener {
        fun onItemClick(view: View, position: Int)
        fun onItemLongClick(view: View, position: Int)
    }

    fun setOnClickListener(clickListener: ClickListener) {
        mClickListener = clickListener
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

        val idNotaItem: TextView = mView.findViewById(R.id.Id_nota_Item)
        val uidUsuarioItem: TextView = mView.findViewById(R.id.Uid_Usuario_Item)
        val correoUsuarioItem: TextView = mView.findViewById(R.id.Correo_usuario_Item)
        val fechaHoraRegistroItem: TextView = mView.findViewById(R.id.Fecha_hora_registro_Item)
        val tituloItem: TextView = mView.findViewById(R.id.Titulo_Item)
        val descripcionItem: TextView = mView.findViewById(R.id.Descripcion_Item)
        val fechaItem: TextView = mView.findViewById(R.id.Fecha_Item)
        val estadoItem: TextView = mView.findViewById(R.id.Estado_Item)
        val tareaFinalizadaItem: CardView = mView.findViewById(R.id.Tarea_Finalizada_Item)
        val tareaNoFinalizadaItem: CardView = mView.findViewById(R.id.Tarea_No_Finalizada_Item)
        val linearEstadoItem: LinearLayout = mView.findViewById(R.id.linearEstado)

        idNotaItem.text = idNota
        uidUsuarioItem.text = uidUsuario
        correoUsuarioItem.text = correoUsuario
        fechaHoraRegistroItem.text = fechaHoraRegistro
        tituloItem.text = titulo
        descripcionItem.text = descripcion
        fechaItem.text = fechaNota
        estadoItem.text = estado

        if (estado.isEmpty()) {

            linearEstadoItem.visibility = View.GONE

        }else{

            linearEstadoItem.visibility = View.VISIBLE

            if (estado.equals("finalizado"))
                tareaFinalizadaItem.visibility = View.VISIBLE
            else
                tareaNoFinalizadaItem.visibility = View.VISIBLE
        }

    }
}