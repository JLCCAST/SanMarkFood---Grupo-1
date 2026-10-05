package com.equipo.sanmarkfood.comensal.presentation.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.comensal.domain.model.auth.ResultadoRegistro
import com.equipo.sanmarkfood.comensal.domain.usecase.auth.RegistrarCuentaUseCase
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
    private val registrarCuenta: RegistrarCuentaUseCase,
    private val gestorSesion: GestorSesion
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun register(name: String, email: String, password: String, acceptedTerms: Boolean) {
        viewModelScope.launch {
            _uiState.value = RegistroUiState(isLoading = true)

            when (val resultado = registrarCuenta(name, email, password, acceptedTerms)) {
                is ResultadoRegistro.Registrado -> {
                    _uiState.value = RegistroUiState()
                    gestorSesion.marcarPorVerificar(resultado.correo, enviadoAhora = true)
                }

                is ResultadoRegistro.CampoInvalido ->
                    _uiState.value = RegistroUiState(error = resultado.mensaje)

                is ResultadoRegistro.Fallo ->
                    _uiState.value = RegistroUiState(
                        error = MapeadorErroresAuth.mapearError(resultado.causa)
                    )
            }
        }
    }

    fun limpiarError() = _uiState.update { it.copy(error = null) }
}