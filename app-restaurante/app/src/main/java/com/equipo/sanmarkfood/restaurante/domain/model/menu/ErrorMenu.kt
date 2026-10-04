package com.equipo.sanmarkfood.restaurante.domain.model.menu

sealed class ErrorMenu : Exception() {
    data object SinConexion : ErrorMenu()
    data object NombreCategoriaInvalido : ErrorMenu()
    data object CategoriaRepetida : ErrorMenu()
    data class DatosPlatoInvalidos(val campos: Set<CampoPlato>) : ErrorMenu()
    data object ImagenIlegible : ErrorMenu()
    data object Desconocido : ErrorMenu()
}
