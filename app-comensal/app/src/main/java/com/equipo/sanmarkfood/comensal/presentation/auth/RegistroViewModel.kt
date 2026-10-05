package com.equipo.sanmarkfood.comensal.presentation.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.data.AuthRepository
import com.equipo.sanmarkfood.comensal.domain.ValidarContrasenaRegistroUseCase
import com.equipo.sanmarkfood.comensal.domain.ValidarCorreoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegistroUiState(
    val isLoading: Boolean = false,
    @StringRes val error: Int? = null
)

@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val repositorio: AuthRepository,
    private val validarCorreo: ValidarCorreoUseCase,
    private val validarContrasenaRegistro: ValidarContrasenaRegistroUseCase,
    private val gestorSesion: GestorSesion
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun register(name: String, email: String, password: String, acceptedTerms: Boolean) {
        val problema = when {
            name.isBlank() -> R.string.error_registro_nombre_vacio
            !validarCorreo(email) -> R.string.error_correo_invalido
            else -> validarContrasenaRegistro(password)
        } ?: if (!acceptedTerms) R.string.error_registro_terminos else null

        if (problema != null) return setError(problema)

        viewModelScope.launch {
            _uiState.value = RegistroUiState(isLoading = true)
            repositorio.register(name.trim(), email.trim(), password)
                .onSuccess {
                    _uiState.value = RegistroUiState()
                    gestorSesion.marcarPorVerificar(email.trim(), enviadoAhora = true)
                }
                .onFailure {
                    _uiState.value = RegistroUiState(
                        error = MapeadorErroresAuth.mapearError(it)
                    )
                }
        }
    }

    fun limpiarError() = _uiState.update { it.copy(error = null) }

    private fun setError(@StringRes mensaje: Int) = _uiState.update { it.copy(error = mensaje) }
}