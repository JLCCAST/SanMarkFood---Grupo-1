package com.equipo.sanmarkfood.restaurante.core.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Arranque

@Serializable
data object InicioSesion

@Serializable
data object Registro

@Serializable
data class VerificarCorreo(val correo: String)

@Serializable
data class PanelProvisional(val administrador: Boolean)

@Serializable
data object DatosLocal

@Serializable
data object HorarioLocal
