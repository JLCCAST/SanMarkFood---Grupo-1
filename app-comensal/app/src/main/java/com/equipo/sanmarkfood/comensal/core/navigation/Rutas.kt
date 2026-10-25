package com.equipo.sanmarkfood.comensal.core.navigation

import kotlinx.serialization.Serializable

@Serializable
data object InicioSesion

@Serializable
data object Registro

@Serializable
data class VerificarCorreo(val correo: String)

@Serializable
data object Invitado

@Serializable
data object PanelComensal

@Serializable
data class DetalleRestaurante(val restauranteId: String)
