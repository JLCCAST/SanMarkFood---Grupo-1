package com.equipo.sanmarkfood.comensal.presentation.reservas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.equipo.sanmarkfood.comensal.core.navigation.DetalleReserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.AnticipacionRecordatorio
import com.equipo.sanmarkfood.comensal.domain.model.reservas.ErrorReservas
import com.equipo.sanmarkfood.comensal.domain.model.reservas.EstadoVisibleReserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.estadoVisible
import com.equipo.sanmarkfood.comensal.domain.usecase.reservas.CancelarReservaUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.reservas.ObservarRecordatoriosUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.reservas.ObservarReservaUseCase
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

data class DetalleReservaUiState(
    val cargando: Boolean = true,
    val sinConexion: Boolean = false,
    val reserva: Reserva? = null,
    val estado: EstadoVisibleReserva? = null,
    val recordatorio: AnticipacionRecordatorio? = null,
    val ahora: Long = 0L,
    val confirmandoCancelar: Boolean = false,
    val cancelando: Boolean = false,
    val error: ErrorReservas? = null,
) {
    val puedeCambiar: Boolean
        get() = estado == EstadoVisibleReserva.PENDIENTE || estado == EstadoVisibleReserva.CONFIRMADA
}

private data class Accion(
    val confirmandoCancelar: Boolean = false,
    val cancelando: Boolean = false,
    val error: ErrorReservas? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DetalleReservaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observarReserva: ObservarReservaUseCase,
    observarRecordatorios: ObservarRecordatoriosUseCase,
    private val cancelarReserva: CancelarReservaUseCase,
) : ViewModel() {

    private val reservaId = savedStateHandle.toRoute<DetalleReserva>().reservaId
    private val accion = MutableStateFlow(Accion())
    private val reintento = MutableStateFlow(0)

    private val reloj = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(MILIS_POR_MINUTO)
        }
    }

    val uiState: StateFlow<DetalleReservaUiState> = reintento.flatMapLatest {
        combine(
            observarReserva(reservaId).map { Result.success(it) }.catch { emit(Result.failure(it)) },
            observarRecordatorios(),
            reloj,
            accion,
        ) { resultado, recordatorios, ahora, accion ->
            val reserva = resultado.getOrNull()
            DetalleReservaUiState(
                cargando = false,
                sinConexion = resultado.isFailure,
                reserva = reserva,
                estado = reserva?.estadoVisible(ahora),
                recordatorio = reserva?.let { recordatorios[it.id] },
                ahora = ahora,
                confirmandoCancelar = accion.confirmandoCancelar,
                cancelando = accion.cancelando,
                error = accion.error,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetalleReservaUiState())

    fun pedirCancelar() = accion.update { it.copy(confirmandoCancelar = true) }

    fun cerrarCancelar() = accion.update { it.copy(confirmandoCancelar = false) }

    fun cerrarError() = accion.update { it.copy(error = null) }

    fun reintentar() = reintento.update { it + 1 }

    fun confirmarCancelar() {
        val reserva = uiState.value.reserva ?: return
        accion.update { it.copy(cancelando = true) }
        viewModelScope.launch {
            try {
                cancelarReserva(reserva)
                accion.update { Accion() }
            } catch (e: ErrorReservas) {
                accion.update { Accion(error = e) }
            }
        }
    }

    private companion object {
        const val MILIS_POR_MINUTO = 60_000L
    }
}
