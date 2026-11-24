package com.equipo.sanmarkfood.restaurante.core.navigation

import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.ModoFormulario
import com.equipo.sanmarkfood.restaurante.presentation.menu.ModoArmarMenu
import com.equipo.sanmarkfood.restaurante.presentation.menu.ModoCamara
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
data object PanelAdministrador

@Serializable
data class RevisarSolicitud(val uid: String)

/** R3 en el alta; O7 «Perfil del local» al editar. */
@Serializable
data class DatosLocal(val modo: ModoFormulario)

/** R4 en el alta; O8 «Horario» al editar. */
@Serializable
data class HorarioLocal(val modo: ModoFormulario)

/** Panel del local con la barra inferior: R5 en «Pedidos» y O6 en «Negocio», por ahora. */
@Serializable
data object PanelLocal

@Serializable
data class Plato(val platoId: String? = null)

@Serializable
data class ArmarMenu(val modo: ModoArmarMenu)

/** M2 con la pizarra del menú del día; M9 con la carta. */
@Serializable
data class Camara(val modo: ModoCamara)

/** M10: la carta leída por la IA, antes de agregarla. */
@Serializable
data object RevisarCarta

@Serializable
data object EntregarCodigo

@Serializable
data class PedidoDetalle(val pedidoId: String)
