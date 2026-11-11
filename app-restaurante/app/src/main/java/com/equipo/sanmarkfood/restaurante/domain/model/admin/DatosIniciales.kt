package com.equipo.sanmarkfood.restaurante.domain.model.admin

enum class RolDePrueba { RESTAURANTE, COMENSAL }

data class CuentaDePrueba(
    val rol: RolDePrueba,
    val nombre: String,
    val correo: String,
)

data class DatosCargados(
    val locales: Int,
    val comensales: Int,
    val platos: Int,
    val pedidos: Int,
    val reservas: Int,
    val resenas: Int,
    val contrasena: String,
    val cuentas: List<CuentaDePrueba>,
)

data class DatosBorrados(
    val cuentas: Int,
    val pedidos: Int,
    val reservas: Int,
)

sealed class ErrorDatosIniciales : Exception() {
    data object SinConexion : ErrorDatosIniciales()
    data object NoAutorizado : ErrorDatosIniciales()
    data object Desconocido : ErrorDatosIniciales()
}
