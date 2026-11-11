package com.equipo.sanmarkfood.comensal.presentation.reservas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.comensal.domain.model.reservas.AnticipacionRecordatorio
import com.equipo.sanmarkfood.comensal.domain.model.reservas.ErrorReservas
import com.equipo.sanmarkfood.comensal.domain.model.reservas.EstadoVisibleReserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.estadoVisible
import com.equipo.sanmarkfood.comensal.domain.model.reservas.proximaReserva
import com.equipo.sanmarkfood.comensal.domain.usecase.reservas.CancelarReservaUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.reservas.LimpiarRecordatoriosUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.reservas.ObservarRecordatoriosUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.reservas.ObservarReservasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReservaEnLista(
    val reserva: Reserva,
    val estado: EstadoVisibleReserva,
    val recordatorio: AnticipacionRecordatorio?,
)

data class ReservasActividadUiState(
    val cargando: Boolean = true,
    val sinConexion: Boolean = false,
    val proxima: ReservaEnLista? = null,
    val reservas: List<ReservaEnLista> = emptyList(),
    val porCancelar: Reserva? = null,
    val cancelando: Boolean = false,
    val error: ErrorReservas? = null,
)

private data class AccionActividad(
    val porCancelar: Reserva? = null,
    val cancelando: Boolean = false,
    val error: ErrorReservas? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReservasActividadViewModel @Inject constructor(
    observarReservas: ObservarReservasUseCase,
    observarRecordatorios: ObservarRecordatoriosUseCase,
    private val cancelarReserva: CancelarReservaUseCase,
    private val limpiarRecordatorios: LimpiarRecordatoriosUseCase,
) : ViewModel() {

    private val reintento = MutableStateFlow(0)
    private val accion = MutableStateFlow(AccionActividad())

    private val reloj = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(MILIS_POR_MINUTO)
        }
    }

    val uiState: StateFlow<ReservasActividadUiState> = reintento.flatMapLatest {
        combine(
            observarReservas().map { Result.success(it) }.catch { emit(Result.failure(it)) },
            observarRecordatorios(),
            reloj,
            accion,
        ) { resultado, recordatorios, ahora, accion ->
            val reservas = resultado.getOrNull()
            if (reservas == null) {
                ReservasActividadUiState(
                    cargando = false,
                    sinConexion = resultado.exceptionOrNull() == ErrorReservas.SinConexion,
                    error = accion.error,
                )
            } else {
                limpiarRecordatorios(reservas, recordatorios)
                val enLista = reservas.map { ReservaEnLista(it, it.estadoVisible(ahora), recordatorios[it.id]) }
                val proxima = reservas.proximaReserva(ahora)
                ReservasActividadUiState(
                    cargando = false,
                    proxima = enLista.firstOrNull { it.reserva.id == proxima?.id },
                    reservas = enLista,
                    porCancelar = accion.porCancelar,
                    cancelando = accion.cancelando,
                    error = accion.error,
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReservasActividadUiState())

    fun reintentar() = reintento.update { it + 1 }

    fun pedirCancelar(reserva: Reserva) = accion.update { it.copy(porCancelar = reserva) }

    fun cerrarCancelar() = accion.update { it.copy(porCancelar = null) }

    fun cerrarError() = accion.update { it.copy(error = null) }

    fun confirmarCancelar() {
        val reserva = accion.value.porCancelar ?: return
        accion.update { it.copy(cancelando = true) }
        viewModelScope.launch {
            try {
                cancelarReserva(reserva)
                accion.update { AccionActividad() }
            } catch (e: ErrorReservas) {
                accion.update { AccionActividad(error = e) }
            }
        }
    }

    private companion object {
        const val MILIS_POR_MINUTO = 60_000L
    }
}
