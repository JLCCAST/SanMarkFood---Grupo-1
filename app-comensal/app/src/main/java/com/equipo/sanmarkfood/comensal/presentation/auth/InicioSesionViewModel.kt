package com.equipo.sanmarkfood.comensal.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.comensal.domain.model.auth.ErrorAuth
import com.equipo.sanmarkfood.comensal.domain.model.auth.ResultadoInicioSesion
import com.equipo.sanmarkfood.comensal.domain.usecase.auth.IniciarSesionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InicioSesionUiState(
    val isLoading: Boolean = false,
    val error: ErrorAuth? = null
)

@HiltViewModel
class InicioSesionViewModel @Inject constructor(
    private val iniciarSesion: IniciarSesionUseCase,
    private val gestorSesion: GestorSesion
) : ViewModel() {

    private val _uiState = MutableStateFlow(InicioSesionUiState())
    val uiState: StateFlow<InicioSesionUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = InicioSesionUiState(isLoading = true)

            when (val resultado = iniciarSesion(email, password)) {
                is ResultadoInicioSesion.Autenticado -> {
                    _uiState.value = InicioSesionUiState()
                    gestorSesion.marcarAutenticado()
                }

                is ResultadoInicioSesion.PorVerificar -> {
                    _uiState.value = InicioSesionUiState()
                    gestorSesion.marcarPorVerificar(resultado.correo)
                }

                is ResultadoInicioSesion.CampoInvalido ->
                    _uiState.value = InicioSesionUiState(error = resultado.error)

                is ResultadoInicioSesion.Fallo ->
                    _uiState.value = InicioSesionUiState(error = resultado.error)
            }
        }
    }

    /** Botón "Explorar sin cuenta": entra como invitado, sin usar Firebase. */
    fun continuarComoInvitado() = gestorSesion.entrarComoInvitado()

    fun limpiarError() = _uiState.update { it.copy(error = null) }
}