package com.equipo.sanmarkfood.restaurante.core.navigation

import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.ModoFormulario
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

/** R3 en el alta; O7 «Perfil del local» al editar. */
@Serializable
data class DatosLocal(val modo: ModoFormulario)

/** R4 en el alta; O8 «Horario» al editar. */
@Serializable
data class HorarioLocal(val modo: ModoFormulario)

/** Panel del local con la barra inferior: R5 en «Pedidos» y O6 en «Negocio», por ahora. */
@Serializable
data object PanelLocal
