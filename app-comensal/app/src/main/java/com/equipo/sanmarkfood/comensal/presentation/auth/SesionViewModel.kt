package com.equipo.sanmarkfood.comensal.presentation.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * ViewModel de AuthGate: expone el estado de sesión para decidir qué pantalla
 * mostrar y permite cerrar sesión o salir del modo invitado.
 */
@HiltViewModel
class SesionViewModel @Inject constructor(
    private val gestorSesion: GestorSesion
) : ViewModel() {

    val estado: StateFlow<EstadoSesion> = gestorSesion.estado

    fun cerrarSesion() = gestorSesion.cerrarSesion()

    fun salirDeInvitado() = gestorSesion.salirDeInvitado()
}