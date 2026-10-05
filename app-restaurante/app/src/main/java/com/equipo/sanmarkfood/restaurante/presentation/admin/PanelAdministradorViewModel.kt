package com.equipo.sanmarkfood.restaurante.presentation.admin

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.usecase.auth.CerrarSesionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class PestanaAdmin(@param:DrawableRes val icono: Int, @param:StringRes val etiqueta: Int) {
    SOLICITUDES(R.drawable.ic_solicitudes, R.string.admin_solicitudes),
    REPORTES(R.drawable.ic_reportes, R.string.admin_reportes),
    METRICAS(R.drawable.ic_metricas, R.string.admin_metricas),
}

data class PanelAdministradorUiState(
    val pestana: PestanaAdmin = PestanaAdmin.SOLICITUDES,
    val sesionCerrada: Boolean = false,
)

@HiltViewModel
class PanelAdministradorViewModel @Inject constructor(
    private val cerrarSesion: CerrarSesionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PanelAdministradorUiState())
    val uiState: StateFlow<PanelAdministradorUiState> = _uiState.asStateFlow()

    fun onElegirPestana(pestana: PestanaAdmin) = _uiState.update { it.copy(pestana = pestana) }

    fun onCerrarSesion() {
        cerrarSesion()
        _uiState.update { it.copy(sesionCerrada = true) }
    }
}
