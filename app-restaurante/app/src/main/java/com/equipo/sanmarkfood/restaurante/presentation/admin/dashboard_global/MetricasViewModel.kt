package com.equipo.sanmarkfood.restaurante.presentation.admin.dashboard_global

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.admin.ErrorAdmin
import com.equipo.sanmarkfood.restaurante.domain.model.admin.MetricasPlataforma
import com.equipo.sanmarkfood.restaurante.domain.usecase.admin.ObtenerMetricasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class MetricasUiState(
    val cargando: Boolean = true,
    val metricas: MetricasPlataforma? = null,
    val actualizadoEn: Date? = null,
    val error: ErrorAdmin? = null,
)

@HiltViewModel
class MetricasViewModel @Inject constructor(
    private val obtenerMetricas: ObtenerMetricasUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MetricasUiState())
    val uiState: StateFlow<MetricasUiState> = _uiState.asStateFlow()

    private var carga: Job? = null

    fun onAbrir() = cargar()

    fun onReintentar() = cargar()

    private fun cargar() {
        if (carga?.isActive == true) return

        _uiState.update { it.copy(cargando = it.metricas == null, error = null) }
        carga = viewModelScope.launch {
            try {
                val metricas = obtenerMetricas()
                _uiState.update { it.copy(cargando = false, metricas = metricas, actualizadoEn = Date()) }
            } catch (e: ErrorAdmin) {
                _uiState.update { it.copy(cargando = false, error = e) }
            }
        }
    }
}
