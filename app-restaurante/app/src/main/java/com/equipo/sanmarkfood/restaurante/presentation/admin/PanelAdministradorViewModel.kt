package com.equipo.sanmarkfood.restaurante.presentation.admin

import androidx.lifecycle.ViewModel
import com.equipo.sanmarkfood.restaurante.domain.usecase.auth.CerrarSesionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class PanelAdministradorUiState(
    val sesionCerrada: Boolean = false,
)

@HiltViewModel
class PanelAdministradorViewModel @Inject constructor(
    private val cerrarSesion: CerrarSesionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PanelAdministradorUiState())
    val uiState: StateFlow<PanelAdministradorUiState> = _uiState.asStateFlow()

    fun onCerrarSesion() {
        cerrarSesion()
        _uiState.update { it.copy(sesionCerrada = true) }
    }
}
