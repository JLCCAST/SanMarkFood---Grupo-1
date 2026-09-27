package com.equipo.sanmarkfood.restaurante.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.EstadoSesion
import com.equipo.sanmarkfood.restaurante.domain.usecase.ObtenerEstadoSesionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArranqueUiState(
    val sesion: EstadoSesion? = null,
)

@HiltViewModel
class ArranqueViewModel @Inject constructor(
    obtenerEstadoSesion: ObtenerEstadoSesionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArranqueUiState())
    val uiState: StateFlow<ArranqueUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val sesion = obtenerEstadoSesion()
            _uiState.update { it.copy(sesion = sesion) }
        }
    }
}
