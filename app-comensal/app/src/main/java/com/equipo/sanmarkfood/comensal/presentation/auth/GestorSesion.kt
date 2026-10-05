package com.equipo.sanmarkfood.comensal.presentation.auth

import com.equipo.sanmarkfood.comensal.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface EstadoSesion {
    data object SinSesion : EstadoSesion
    data class PorVerificar(
        val correo: String,
        val enviadoAhora: Boolean = false
    ) : EstadoSesion
    data object Autenticado : EstadoSesion
    data object Invitado : EstadoSesion
}

/**
 * Única fuente de verdad del estado de sesión. Lo observa AuthGate para decidir
 * qué pantalla mostrar, y lo modifican los ViewModels de cada pantalla de auth.
 */
@Singleton
class GestorSesion @Inject constructor(
    private val repositorio: AuthRepository
) {
    private val _estado = MutableStateFlow(calcularEstadoInicial())
    val estado: StateFlow<EstadoSesion> = _estado.asStateFlow()

    private fun calcularEstadoInicial(): EstadoSesion {
        val usuario = repositorio.currentUser ?: return EstadoSesion.SinSesion
        return if (usuario.correoVerificado) {
            EstadoSesion.Autenticado
        } else {
            EstadoSesion.PorVerificar(usuario.correo)
        }
    }

    fun marcarAutenticado() {
        _estado.value = EstadoSesion.Autenticado
    }

    /** enviadoAhora = true cuando el correo de verificación se acaba de enviar. */
    fun marcarPorVerificar(correo: String, enviadoAhora: Boolean = false) {
        _estado.value = EstadoSesion.PorVerificar(correo, enviadoAhora)
    }

    fun entrarComoInvitado() {
        _estado.value = EstadoSesion.Invitado
    }

    /** Sale del modo invitado sin tocar Firebase (equivale a exitGuest). */
    fun salirDeInvitado() {
        _estado.value = EstadoSesion.SinSesion
    }

    /** Cierra la sesión en Firebase y vuelve al formulario (logout y changeEmail). */
    fun cerrarSesion() {
        repositorio.logout()
        _estado.value = EstadoSesion.SinSesion
    }
}