package com.equipo.sanmarkfood.restaurante.presentation.dashboard

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.auth.ErrorAuth
import com.equipo.sanmarkfood.restaurante.domain.usecase.auth.CerrarSesionUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.auth.EliminarCuentaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NegocioUiState(
    val sesionCerrada: Boolean = false,
    val confirmandoEliminacion: Boolean = false,
    val eliminando: Boolean = false,
    @param:StringRes val errorEliminacion: Int? = null,
)

@HiltViewModel
class NegocioViewModel @Inject constructor(
    private val cerrarSesion: CerrarSesionUseCase,
    private val eliminarCuenta: EliminarCuentaUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NegocioUiState())
    val uiState: StateFlow<NegocioUiState> = _uiState.asStateFlow()

    fun onCerrarSesion() {
        cerrarSesion()
        _uiState.update { it.copy(sesionCerrada = true) }
    }

    fun onPedirEliminacion() {
        _uiState.update { it.copy(confirmandoEliminacion = true, errorEliminacion = null) }
    }

    fun onCancelarEliminacion() {
        if (_uiState.value.eliminando) return
        _uiState.update { it.copy(confirmandoEliminacion = false, errorEliminacion = null) }
    }

    fun onEliminarCuenta() {
        if (_uiState.value.eliminando) return
        _uiState.update { it.copy(eliminando = true, errorEliminacion = null) }
        viewModelScope.launch {
            try {
                eliminarCuenta()
                _uiState.update { it.copy(eliminando = false, sesionCerrada = true) }
            } catch (e: ErrorAuth) {
                val mensaje = if (e == ErrorAuth.SinConexion) R.string.error_sin_conexion else R.string.negocio_eliminar_error
                _uiState.update { it.copy(eliminando = false, errorEliminacion = mensaje) }
            }
        }
    }
}
