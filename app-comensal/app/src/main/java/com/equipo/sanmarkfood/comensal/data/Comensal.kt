package com.equipo.sanmarkfood.comensal.data

data class Comensal(
    val uid: String = "",
    val nombre: String = "",
    val telefono: String = "",
    val fotoUrl: String = "",
    val direcciones: List<Direccion> = emptyList(),
    val notificarReservas: Boolean = true,
    val notificarResenas: Boolean = true
)

data class Direccion(
    val etiqueta: String = "",
    val direccionTexto: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
)