package com.equipo.sanmarkfood.restaurante.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.ErrorAuth
import com.equipo.sanmarkfood.restaurante.domain.model.EstadoSesion
import com.equipo.sanmarkfood.restaurante.domain.usecase.IniciarSesionUseCase
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
    val error: ErrorAuth? = null,
    val sesion: EstadoSesion? = null,
)

@HiltViewModel
class InicioSesionViewModel @Inject constructor(
    private val iniciarSesion: IniciarSesionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InicioSesionUiState())
    val uiState: StateFlow<InicioSesionUiState> = _uiState.asStateFlow()

    fun onCambiarCorreo(correo: String) = _uiState.update { it.copy(correo = correo, error = null) }

    fun onCambiarContrasena(contrasena: String) =
        _uiState.update { it.copy(contrasena = contrasena, error = null) }

    fun onIniciarSesion() {
        val estado = _uiState.value
        if (estado.cargando) return

        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try {
                val sesion = iniciarSesion(estado.correo, estado.contrasena)
                _uiState.update { it.copy(cargando = false, sesion = sesion) }
            } catch (e: ErrorAuth) {
                _uiState.update { it.copy(cargando = false, error = e) }
            }
        }
    }
}
