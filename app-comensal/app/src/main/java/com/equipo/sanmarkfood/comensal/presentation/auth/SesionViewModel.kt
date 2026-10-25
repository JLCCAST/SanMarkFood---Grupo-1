package com.equipo.sanmarkfood.comensal.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.comensal.domain.usecase.auth.OlvidarDispositivoUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.auth.RegistrarDispositivoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel de AuthGate: expone el estado de sesión para decidir qué pantalla
 * mostrar y permite cerrar sesión o salir del modo invitado.
 */
@HiltViewModel
class SesionViewModel @Inject constructor(
    private val gestorSesion: GestorSesion,
    private val registrarDispositivo: RegistrarDispositivoUseCase,
    private val olvidarDispositivo: OlvidarDispositivoUseCase
) : ViewModel() {

    val estado: StateFlow<EstadoSesion> = gestorSesion.estado

    init {
        viewModelScope.launch {
            estado.collect { sesion ->
                if (sesion == EstadoSesion.Autenticado) registrarDispositivo()
            }
        }
    }

    fun cerrarSesion() {
        olvidarDispositivo()
        gestorSesion.cerrarSesion()
    }

    fun salirDeInvitado() = gestorSesion.salirDeInvitado()
}
