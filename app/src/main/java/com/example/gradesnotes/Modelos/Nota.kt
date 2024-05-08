package com.example.gradesnotes.Modelos

data class Nota(
    var idNota: String = "",
    var uidUsuario: String = "",
    var correoUsuario: String = "",
    var fechaHoraActual: String = "",
    var titulo: String = "",
    var descripcion: String = "",
    var fechaNota: String = "",
    var estado: String = ""
)