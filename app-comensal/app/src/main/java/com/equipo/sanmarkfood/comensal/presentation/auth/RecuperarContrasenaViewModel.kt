package com.equipo.sanmarkfood.comensal.presentation.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.comensal.domain.usecase.auth.ErrorValidacion
import com.equipo.sanmarkfood.comensal.domain.usecase.auth.RecuperarContrasenaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecuperarContrasenaUiState(
    val isLoading: Boolean = false,
    @StringRes val error: Int? = null,
    val enlaceEnviado: Boolean = false
)

@HiltViewModel
class RecuperarContrasenaViewModel @Inject constructor(
    private val recuperarContrasena: RecuperarContrasenaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecuperarContrasenaUiState())
    val uiState: StateFlow<RecuperarContrasenaUiState> = _uiState.asStateFlow()

    /** Botón "Enviar enlace" del diálogo de recuperar contraseña. */
    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.value = RecuperarContrasenaUiState(isLoading = true)
            recuperarContrasena(email)
                .onSuccess {
                    _uiState.value = RecuperarContrasenaUiState(enlaceEnviado = true)
                }
                .onFailure { e ->
                    val mensaje = when (e) {
                        is ErrorValidacion -> e.mensajeRes
                        else -> MapeadorErroresAuth.mapearErrorRecuperacion(e)
                    }
                    _uiState.value = RecuperarContrasenaUiState(error = mensaje)
                }
        }
    }

    /** Limpia el estado del diálogo al abrirlo o cerrarlo. */
    fun limpiar() {
        _uiState.value = RecuperarContrasenaUiState()
    }
}