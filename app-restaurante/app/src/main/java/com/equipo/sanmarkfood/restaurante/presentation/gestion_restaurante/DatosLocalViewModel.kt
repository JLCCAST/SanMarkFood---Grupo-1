package com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante

import androidx.lifecycle.ViewModel
import com.equipo.sanmarkfood.restaurante.domain.usecase.CerrarSesionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class DatosLocalUiState(
    val sesionCerrada: Boolean = false,
)

@HiltViewModel
class DatosLocalViewModel @Inject constructor(
    private val cerrarSesion: CerrarSesionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DatosLocalUiState())
    val uiState: StateFlow<DatosLocalUiState> = _uiState.asStateFlow()

    // La cuenta ya está creada y verificada: volver al paso 1 no tiene sentido, así que
    // «atrás» cierra la sesión. Al volver a entrar, el arranque trae de nuevo a este paso.
    fun onVolver() {
        cerrarSesion()
        _uiState.update { it.copy(sesionCerrada = true) }
    }
}
