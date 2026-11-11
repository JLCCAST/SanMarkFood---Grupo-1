package com.equipo.sanmarkfood.restaurante.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface DatosInicialesApi {
    @POST("datosIniciales")
    suspend fun datosIniciales(@Body accion: AccionDatosInicialesDto): DatosInicialesDto
}

@Serializable
data class AccionDatosInicialesDto(val accion: String)

@Serializable
data class DatosInicialesDto(
    val locales: Int = 0,
    val comensales: Int = 0,
    val platos: Int = 0,
    val pedidos: Int = 0,
    val reservas: Int = 0,
    val resenas: Int = 0,
    val cuentas: List<CuentaDePruebaDto>? = null,
    val cuentasBorradas: Int = 0,
    val contrasena: String = "",
)

@Serializable
data class CuentaDePruebaDto(
    val rol: String,
    val nombre: String,
    val correo: String,
)
