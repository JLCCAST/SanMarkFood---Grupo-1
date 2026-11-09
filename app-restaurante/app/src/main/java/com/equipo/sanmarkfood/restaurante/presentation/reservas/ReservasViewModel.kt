package com.equipo.sanmarkfood.restaurante.presentation.reservas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.menu.fechaDentroDe
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.DIAS_VISIBLES
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.ErrorReservas
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.EstadoReserva
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.restaurante.domain.usecase.reservas.ObservarReservasDeLaSemanaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReservasUiState(
    val cargando: Boolean = true,
    val dias: List<String> = List(DIAS_VISIBLES) { fechaDentroDe(it) },
    val diaElegido: String = dias.first(),
    val reservas: List<Reserva> = emptyList(),
    val sinConexion: Boolean = false,
    val error: ErrorReservas? = null,
) {
    val delDia: List<Reserva> get() = reservas.filter { it.fecha == diaElegido }

    val pendientes: List<Reserva> get() = delDia.filter { it.estado == EstadoReserva.PENDIENTE }

    fun tienePendientes(fecha: String): Boolean =
        reservas.any { it.fecha == fecha && it.estado == EstadoReserva.PENDIENTE }
}

@HiltViewModel
class ReservasViewModel @Inject constructor(
    private val observarReservasDeLaSemana: ObservarReservasDeLaSemanaUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservasUiState())
    val uiState: StateFlow<ReservasUiState> = _uiState.asStateFlow()

    private var observacion: Job? = null

    init {
        observar()
    }

    fun onReintentar() = observar()

    fun onElegirDia(fecha: String) = _uiState.update { it.copy(diaElegido = fecha) }

    private fun observar() {
        observacion?.cancel()
        _uiState.update { it.copy(cargando = true, error = null) }
        observacion = viewModelScope.launch {
            observarReservasDeLaSemana()
                .catch { e -> if (e is ErrorReservas) _uiState.update { it.copy(cargando = false, error = e) } else throw e }
                .collect { semana ->
                    _uiState.update {
                        it.copy(cargando = false, reservas = semana.reservas, sinConexion = semana.sinConexion, error = null)
                    }
                }
        }
    }
}
