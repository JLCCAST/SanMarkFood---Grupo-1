package com.equipo.sanmarkfood.restaurante.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.auth.ErrorAuth
import com.equipo.sanmarkfood.restaurante.domain.model.auth.EstadoSesion
import com.equipo.sanmarkfood.restaurante.domain.usecase.auth.ObtenerEstadoSesionUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.auth.RegistrarDispositivoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArranqueUiState(
    val sesion: EstadoSesion? = null,
    val error: ErrorAuth? = null,
)

@HiltViewModel
class ArranqueViewModel @Inject constructor(
    private val obtenerEstadoSesion: ObtenerEstadoSesionUseCase,
    private val registrarDispositivo: RegistrarDispositivoUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArranqueUiState())
    val uiState: StateFlow<ArranqueUiState> = _uiState.asStateFlow()

    init {
        leerSesion()
    }

    fun onReintentar() = leerSesion()

    private fun leerSesion() {
        _uiState.update { ArranqueUiState() }
        viewModelScope.launch {
            try {
                val sesion = obtenerEstadoSesion()
                registrarDispositivo(sesion)
                _uiState.update { it.copy(sesion = sesion) }
            } catch (e: ErrorAuth) {
                _uiState.update { it.copy(error = e) }
            }
        }
    }
}
