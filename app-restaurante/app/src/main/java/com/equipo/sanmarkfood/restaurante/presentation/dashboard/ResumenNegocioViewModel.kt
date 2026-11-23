package com.equipo.sanmarkfood.restaurante.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.EvolucionCalificacion
import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.PeriodoNegocio
import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.ResumenNegocio
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.usecase.dashboard.ObtenerEvolucionCalificacionUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.dashboard.ObtenerResumenNegocioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResumenNegocioUiState(
    val periodo: PeriodoNegocio = PeriodoNegocio.SEMANA,
    val resumenes: Map<PeriodoNegocio, ResumenNegocio> = emptyMap(),
    val calificacion: EvolucionCalificacion? = null,
    val error: ErrorRestaurante? = null,
) {
    val resumen: ResumenNegocio?
        get() = resumenes[periodo]
}

@HiltViewModel
class ResumenNegocioViewModel @Inject constructor(
    private val obtenerResumen: ObtenerResumenNegocioUseCase,
    private val obtenerCalificacion: ObtenerEvolucionCalificacionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResumenNegocioUiState())
    val uiState: StateFlow<ResumenNegocioUiState> = _uiState.asStateFlow()

    private var carga: Job? = null
    private var cargaCalificacion: Job? = null

    fun onAbrir() = cargarTodo()

    fun onReintentar() = cargarTodo()

    fun onElegirPeriodo(periodo: PeriodoNegocio) {
        if (periodo == _uiState.value.periodo) return
        _uiState.update { it.copy(periodo = periodo) }
        cargarResumen()
    }

    private fun cargarTodo() {
        cargarResumen()
        cargarCalificacion()
    }

    private fun cargarResumen() {
        val periodo = _uiState.value.periodo
        carga?.cancel()
        _uiState.update { it.copy(error = null) }
        carga = viewModelScope.launch {
            try {
                val resumen = obtenerResumen(periodo)
                _uiState.update { it.copy(resumenes = it.resumenes + (periodo to resumen)) }
            } catch (e: ErrorRestaurante) {
                _uiState.update { it.copy(error = e) }
            }
        }
    }

    private fun cargarCalificacion() {
        cargaCalificacion?.cancel()
        cargaCalificacion = viewModelScope.launch {
            try {
                val calificacion = obtenerCalificacion()
                _uiState.update { it.copy(calificacion = calificacion) }
            } catch (e: ErrorRestaurante) {
                _uiState.update { it.copy(error = e) }
            }
        }
    }
}
