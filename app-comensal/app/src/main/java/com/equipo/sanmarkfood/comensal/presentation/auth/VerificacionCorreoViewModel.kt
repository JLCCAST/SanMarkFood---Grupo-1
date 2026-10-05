package com.equipo.sanmarkfood.comensal.presentation.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VerificacionCorreoUiState(
    val isLoading: Boolean = false,
    @StringRes val error: Int? = null,
    val resendCooldown: Int = 0
)

@HiltViewModel
class VerificacionCorreoViewModel @Inject constructor(
    private val repositorio: AuthRepository,
    private val gestorSesion: GestorSesion
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerificacionCorreoUiState())
    val uiState: StateFlow<VerificacionCorreoUiState> = _uiState.asStateFlow()

    private var esperaJob: Job? = null

    init {
        viewModelScope.launch {
            gestorSesion.estado.collect { sesion ->
                if (sesion is EstadoSesion.PorVerificar && sesion.enviadoAhora) {
                    iniciarEspera()
                }
            }
        }
    }

    /** Botón "Ya verifiqué mi correo". */
    fun checkVerified() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repositorio.reloadAndCheckVerified()
                .onSuccess { verificado ->
                    if (verificado) {
                        esperaJob?.cancel()
                        _uiState.value = VerificacionCorreoUiState()
                        gestorSesion.marcarAutenticado()
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = R.string.error_verificacion_pendiente
                            )
                        }
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = MapeadorErroresAuth.mapearError(e))
                    }
                }
        }
    }

    /** Botón "Reenviar correo" (bloqueado mientras corre la espera de 60 s). */
    fun resendVerification() {
        if (_uiState.value.resendCooldown > 0) return
        viewModelScope.launch {
            repositorio.sendVerification()
                .onSuccess {
                    _uiState.update { it.copy(error = null) }
                    iniciarEspera()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = MapeadorErroresAuth.mapearError(e)) }
                }
        }
    }

    /** Botón "Cambiar correo": cierra la sesión pendiente y vuelve al formulario. */
    fun changeEmail() {
        esperaJob?.cancel()
        _uiState.value = VerificacionCorreoUiState()
        gestorSesion.cerrarSesion()
    }

    private fun iniciarEspera(segundos: Int = 60) {
        esperaJob?.cancel()
        esperaJob = viewModelScope.launch {
            for (restante in segundos downTo 1) {
                _uiState.update { it.copy(resendCooldown = restante) }
                delay(1000)
            }
            _uiState.update { it.copy(resendCooldown = 0) }
        }
    }
}