package com.equipo.sanmarkfood.comensal.presentation.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.data.AuthRepository
import com.equipo.sanmarkfood.comensal.domain.ValidarCorreoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InicioSesionUiState(
    val isLoading: Boolean = false,
    @StringRes val error: Int? = null
)

@HiltViewModel
class InicioSesionViewModel @Inject constructor(
    private val repositorio: AuthRepository,
    private val validarCorreo: ValidarCorreoUseCase,
    private val gestorSesion: GestorSesion
) : ViewModel() {

    private val _uiState = MutableStateFlow(InicioSesionUiState())
    val uiState: StateFlow<InicioSesionUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isEmpty()) {
            return setError(R.string.error_login_campos_vacios)
        }
        if (!validarCorreo(email)) return setError(R.string.error_correo_invalido)

        viewModelScope.launch {
            _uiState.value = InicioSesionUiState(isLoading = true)
            repositorio.login(email.trim(), password)
                .onSuccess { user ->
                    _uiState.value = InicioSesionUiState()
                    if (user.isEmailVerified) {
                        gestorSesion.marcarAutenticado()
                    } else {
                        gestorSesion.marcarPorVerificar(user.email ?: email.trim())
                    }
                }
                .onFailure {
                    _uiState.value = InicioSesionUiState(
                        error = MapeadorErroresAuth.mapearError(it)
                    )
                }
        }
    }

    /** Botón "Explorar sin cuenta": entra como invitado, sin usar Firebase. */
    fun continuarComoInvitado() = gestorSesion.entrarComoInvitado()

    fun limpiarError() = _uiState.update { it.copy(error = null) }

    private fun setError(@StringRes mensaje: Int) = _uiState.update { it.copy(error = mensaje) }
}