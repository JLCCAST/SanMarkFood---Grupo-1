package com.equipo.sanmarkfood.restaurante.presentation.reservas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.menu.fechaDentroDe
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.DIAS_VISIBLES
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.ErrorReservas
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.EstadoReserva
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.MotivoRechazoReserva
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.restaurante.domain.usecase.reservas.ConfirmarReservaUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.reservas.ObservarReservasDeLaSemanaUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.reservas.RechazarReservaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HojaRechazarReserva(
    val reserva: Reserva,
    val motivo: MotivoRechazoReserva? = null,
    val detalle: String = "",
    val enviando: Boolean = false,
    val error: ErrorReservas? = null,
) {
    val completa: Boolean
        get() = when (motivo) {
            null -> false
            MotivoRechazoReserva.OTRO -> detalle.trim().length >= 3
            else -> true
        }
}

sealed interface AvisoReservas {
    data class Confirmada(val comensal: String) : AvisoReservas
    data object Rechazada : AvisoReservas
    data class Fallo(val error: ErrorReservas) : AvisoReservas
}

data class ReservasUiState(
    val cargando: Boolean = true,
    val dias: List<String> = List(DIAS_VISIBLES) { fechaDentroDe(it) },
    val diaElegido: String = dias.first(),
    val reservas: List<Reserva> = emptyList(),
    val sinConexion: Boolean = false,
    val error: ErrorReservas? = null,
    val hojaRechazar: HojaRechazarReserva? = null,
    val procesando: Set<String> = emptySet(),
    val aviso: AvisoReservas? = null,
) {
    val delDia: List<Reserva> get() = reservas.filter { it.fecha == diaElegido }

    val pendientes: List<Reserva> get() = delDia.filter { it.estado == EstadoReserva.PENDIENTE }

    fun tienePendientes(fecha: String): Boolean =
        reservas.any { it.fecha == fecha && it.estado == EstadoReserva.PENDIENTE }
}

@HiltViewModel
class ReservasViewModel @Inject constructor(
    private val observarReservasDeLaSemana: ObservarReservasDeLaSemanaUseCase,
    private val confirmarReserva: ConfirmarReservaUseCase,
    private val rechazarReserva: RechazarReservaUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservasUiState())
    val uiState: StateFlow<ReservasUiState> = _uiState.asStateFlow()

    private var observacion: Job? = null

    init {
        observar()
    }

    fun onReintentar() = observar()

    fun onElegirDia(fecha: String) = _uiState.update { it.copy(diaElegido = fecha) }

    fun onConfirmar(reserva: Reserva) {
        if (reserva.id in _uiState.value.procesando) return

        _uiState.update { it.copy(procesando = it.procesando + reserva.id) }
        viewModelScope.launch {
            val aviso = try {
                confirmarReserva(reserva)
                AvisoReservas.Confirmada(reserva.comensalNombre)
            } catch (e: ErrorReservas) {
                AvisoReservas.Fallo(e)
            }
            _uiState.update { it.copy(procesando = it.procesando - reserva.id, aviso = aviso) }
        }
    }

    fun onRechazar(reserva: Reserva) = _uiState.update { it.copy(hojaRechazar = HojaRechazarReserva(reserva)) }

    fun onElegirMotivo(motivo: MotivoRechazoReserva) = cambiarHoja { it.copy(motivo = motivo) }

    fun onCambiarDetalle(detalle: String) = cambiarHoja { it.copy(detalle = detalle) }

    fun onCerrarHoja() = _uiState.update { it.copy(hojaRechazar = null) }

    fun onConfirmarRechazo() {
        val hoja = _uiState.value.hojaRechazar ?: return
        val motivo = hoja.motivo ?: return
        if (hoja.enviando || !hoja.completa) return

        _uiState.update { it.copy(hojaRechazar = hoja.copy(enviando = true, error = null)) }
        viewModelScope.launch {
            try {
                rechazarReserva(hoja.reserva, motivo, hoja.detalle)
                _uiState.update { it.copy(hojaRechazar = null, aviso = AvisoReservas.Rechazada) }
            } catch (e: ErrorReservas) {
                _uiState.update { it.copy(hojaRechazar = it.hojaRechazar?.copy(enviando = false, error = e)) }
            }
        }
    }

    fun onAvisoMostrado() = _uiState.update { it.copy(aviso = null) }

    private fun cambiarHoja(cambio: (HojaRechazarReserva) -> HojaRechazarReserva) =
        _uiState.update { estado -> estado.copy(hojaRechazar = estado.hojaRechazar?.let { cambio(it).copy(error = null) }) }

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
