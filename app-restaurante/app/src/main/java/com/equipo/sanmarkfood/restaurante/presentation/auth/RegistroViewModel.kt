package com.equipo.sanmarkfood.restaurante.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.ErrorAuth
import com.equipo.sanmarkfood.restaurante.domain.usecase.RegistrarCuentaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MINIMO_CARACTERES = 8

data class RegistroUiState(
    val correo: String = "",
    val contrasena: String = "",
    val repetirContrasena: String = "",
    val cargando: Boolean = false,
    val error: ErrorAuth? = null,
    val cuentaCreada: Boolean = false,
) {
    val contrasenaValida: Boolean get() = contrasena.length >= MINIMO_CARACTERES
    val noCoinciden: Boolean get() = repetirContrasena.isNotEmpty() && repetirContrasena != contrasena
    val puedeCrear: Boolean
        get() = correo.contains('@') && contrasenaValida && repetirContrasena == contrasena
}

@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val registrarCuenta: RegistrarCuentaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun onCambiarCorreo(correo: String) = _uiState.update { it.copy(correo = correo, error = null) }

    fun onCambiarContrasena(contrasena: String) =
        _uiState.update { it.copy(contrasena = contrasena, error = null) }

    fun onCambiarRepetirContrasena(contrasena: String) =
        _uiState.update { it.copy(repetirContrasena = contrasena, error = null) }

    fun onCrearCuenta() {
        val estado = _uiState.value
        if (!estado.puedeCrear || estado.cargando) return

        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try {
                registrarCuenta(estado.correo, estado.contrasena)
                _uiState.update { it.copy(cargando = false, cuentaCreada = true) }
            } catch (e: ErrorAuth) {
                _uiState.update { it.copy(cargando = false, error = e) }
            }
        }
    }
}
