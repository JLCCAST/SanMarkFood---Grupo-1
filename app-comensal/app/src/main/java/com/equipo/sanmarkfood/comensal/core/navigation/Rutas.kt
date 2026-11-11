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

@Serializable
data object TuPedido

@Serializable
data class SeguimientoPedido(val pedidoId: String)

@Serializable
data class ReservarMesa(val restauranteId: String)

@Serializable
data class DetalleReserva(val reservaId: String)

@Serializable
data class ModificarReserva(val reservaId: String)
