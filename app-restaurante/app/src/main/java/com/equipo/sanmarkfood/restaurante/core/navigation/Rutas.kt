package com.equipo.sanmarkfood.restaurante.core.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Registro

@Serializable
data class VerificarCorreo(val correo: String)

@Serializable
data object PanelProvisional
