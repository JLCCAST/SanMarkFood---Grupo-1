package com.equipo.sanmarkfood.comensal.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.comensal.data.AuthRepository
import com.equipo.sanmarkfood.comensal.domain.ValidarCorreoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecuperarContrasenaUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val enlaceEnviado: Boolean = false
)

@HiltViewModel
class RecuperarContrasenaViewModel @Inject constructor(
    private val repositorio: AuthRepository,
    private val validarCorreo: ValidarCorreoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecuperarContrasenaUiState())
    val uiState: StateFlow<RecuperarContrasenaUiState> = _uiState.asStateFlow()

    /** Botón "Enviar enlace" del diálogo de recuperar contraseña. */
    fun sendPasswordReset(email: String) {
        if (email.isBlank()) return setError("Escribe tu correo")
        if (!validarCorreo(email)) return setError("Correo no válido")

        viewModelScope.launch {
            _uiState.value = RecuperarContrasenaUiState(isLoading = true)
            repositorio.sendPasswordReset(email.trim())
                .onSuccess {
                    _uiState.value = RecuperarContrasenaUiState(enlaceEnviado = true)
                }
                .onFailure { e ->
                    _uiState.value = RecuperarContrasenaUiState(
                        error = MapeadorErroresAuth.mapearErrorRecuperacion(e)
                    )
                }
        }
    }

    /** Limpia el estado del diálogo al abrirlo o cerrarlo. */
    fun limpiar() {
        _uiState.value = RecuperarContrasenaUiState()
    }

    private fun setError(mensaje: String) = _uiState.update { it.copy(error = mensaje) }
}