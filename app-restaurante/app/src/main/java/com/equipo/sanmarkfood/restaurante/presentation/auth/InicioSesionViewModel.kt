package com.equipo.sanmarkfood.restaurante.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.auth.ErrorAuth
import com.equipo.sanmarkfood.restaurante.domain.model.auth.EstadoSesion
import com.equipo.sanmarkfood.restaurante.domain.usecase.auth.IniciarSesionConGoogleUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.auth.IniciarSesionUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.auth.RecuperarContrasenaUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.auth.RegistrarDispositivoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InicioSesionUiState(
    val correo: String = "",
    val contrasena: String = "",
    val cargando: Boolean = false,
    val cargandoGoogle: Boolean = false,
    val error: ErrorAuth? = null,
    val sesion: EstadoSesion? = null,
    val recuperacion: RecuperacionUiState? = null,
)

data class RecuperacionUiState(
    val enviando: Boolean = false,
    val enviado: Boolean = false,
    val error: ErrorAuth? = null,
)

@HiltViewModel
class InicioSesionViewModel @Inject constructor(
    private val iniciarSesion: IniciarSesionUseCase,
    private val iniciarSesionConGoogle: IniciarSesionConGoogleUseCase,
    private val recuperarContrasena: RecuperarContrasenaUseCase,
    private val registrarDispositivo: RegistrarDispositivoUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InicioSesionUiState())
    val uiState: StateFlow<InicioSesionUiState> = _uiState.asStateFlow()

    fun onCambiarCorreo(correo: String) = _uiState.update { it.copy(correo = correo, error = null) }

    fun onCambiarContrasena(contrasena: String) =
        _uiState.update { it.copy(contrasena = contrasena, error = null) }

    fun onIniciarSesion() {
        val estado = _uiState.value
        if (estado.cargando || estado.cargandoGoogle) return

        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try {
                val sesion = iniciarSesion(estado.correo, estado.contrasena)
                registrarDispositivo(sesion)
                _uiState.update { it.copy(cargando = false, sesion = sesion) }
            } catch (e: ErrorAuth) {
                _uiState.update { it.copy(cargando = false, error = e) }
            }
        }
    }

    fun onContinuarConGoogle() {
        val estado = _uiState.value
        if (estado.cargando || estado.cargandoGoogle) return

        _uiState.update { it.copy(cargandoGoogle = true, error = null) }
        viewModelScope.launch {
            try {
                val sesion = iniciarSesionConGoogle()
                if (sesion != null) registrarDispositivo(sesion)
                _uiState.update { it.copy(cargandoGoogle = false, sesion = sesion) }
            } catch (e: ErrorAuth) {
                _uiState.update { it.copy(cargandoGoogle = false, error = e) }
            }
        }
    }

    fun onAbrirRecuperacion() = _uiState.update { it.copy(recuperacion = RecuperacionUiState()) }

    fun onCerrarRecuperacion() = _uiState.update { it.copy(recuperacion = null) }

    fun onEnviarEnlace() {
        val estado = _uiState.value
        val recuperacion = estado.recuperacion ?: return
        if (estado.correo.isBlank() || recuperacion.enviando) return

        _uiState.update { it.copy(recuperacion = RecuperacionUiState(enviando = true)) }
        viewModelScope.launch {
            try {
                recuperarContrasena(estado.correo)
                _uiState.update { it.copy(recuperacion = RecuperacionUiState(enviado = true)) }
            } catch (e: ErrorAuth) {
                _uiState.update { it.copy(recuperacion = RecuperacionUiState(error = e)) }
            }
        }
    }
}
