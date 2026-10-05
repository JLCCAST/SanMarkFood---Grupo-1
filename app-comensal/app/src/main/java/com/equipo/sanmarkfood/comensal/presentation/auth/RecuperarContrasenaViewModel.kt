package com.equipo.sanmarkfood.comensal.presentation.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.repository.AuthRepository
import com.equipo.sanmarkfood.comensal.domain.usecase.auth.ValidarCorreoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecuperarContrasenaUiState(
    val isLoading: Boolean = false,
    @StringRes val error: Int? = null,
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
        if (email.isBlank()) return setError(R.string.error_recuperar_correo_vacio)
        if (!validarCorreo(email)) return setError(R.string.error_correo_invalido)

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

    private fun setError(@StringRes mensaje: Int) = _uiState.update { it.copy(error = mensaje) }
}