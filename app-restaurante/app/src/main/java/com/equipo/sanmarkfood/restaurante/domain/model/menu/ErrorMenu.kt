package com.equipo.sanmarkfood.restaurante.domain.model.menu

sealed class ErrorMenu : Exception() {
    data object SinConexion : ErrorMenu()
    data object NombreCategoriaInvalido : ErrorMenu()
    data object CategoriaRepetida : ErrorMenu()
    data object Desconocido : ErrorMenu()
}
