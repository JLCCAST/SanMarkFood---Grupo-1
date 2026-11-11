package com.equipo.sanmarkfood.comensal.presentation.reservas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.equipo.sanmarkfood.comensal.core.navigation.ModificarReserva
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Restaurante
import com.equipo.sanmarkfood.comensal.domain.model.reservas.AnticipacionRecordatorio
import com.equipo.sanmarkfood.comensal.domain.model.reservas.DiaReservable
import com.equipo.sanmarkfood.comensal.domain.model.reservas.ErrorReservas
import com.equipo.sanmarkfood.comensal.domain.model.reservas.HorarioReservable
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.diasReservables
import com.equipo.sanmarkfood.comensal.domain.model.reservas.fechaLimaDe
import com.equipo.sanmarkfood.comensal.domain.model.reservas.horariosDelDia
import com.equipo.sanmarkfood.comensal.domain.model.reservas.primeraHoraDeHoy
import com.equipo.sanmarkfood.comensal.domain.usecase.descubrimiento.ObservarRestauranteUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.reservas.ModificarReservaUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.reservas.ObservarOcupacionUseCase
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CambioGuardado(
    val anterior: Reserva,
    val fecha: String,
    val hora: String,
    val recordatorio: AnticipacionRecordatorio?,
)

data class ModificarReservaUiState(
    val cargando: Boolean = true,
    val sinConexion: Boolean = false,
    val reserva: Reserva? = null,
    val restaurante: Restaurante? = null,
    val dias: List<DiaReservable> = emptyList(),
    val fecha: String? = null,
    val horarios: List<HorarioReservable> = emptyList(),
    val hora: String? = null,
    val primeraHoraDeHoy: String? = null,
    val recordatorio: AnticipacionRecordatorio? = null,
    val confirmando: Boolean = false,
    val guardando: Boolean = false,
    val error: ErrorReservas? = null,
    val guardado: CambioGuardado? = null,
) {
    val sinCambios: Boolean
        get() = reserva == null || (fecha == reserva.fecha && hora == reserva.hora)

    val horaElegida: String?
        get() = hora?.takeIf { elegida -> horarios.any { it.hora == elegida && !it.lleno } }

    val puedeGuardar: Boolean
        get() = reserva?.vigente == true && !sinCambios && fecha != null && horaElegida != null && !guardando
}

private data class EleccionCambio(
    val fecha: String? = null,
    val hora: String? = null,
    val confirmando: Boolean = false,
    val guardando: Boolean = false,
    val error: ErrorReservas? = null,
    val guardado: CambioGuardado? = null,
)

private data class DatosCambio(
    val reserva: Reserva?,
    val restaurante: Restaurante?,
    val recordatorio: AnticipacionRecordatorio?,
    val ahora: Long,
    val sinConexion: Boolean,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ModificarReservaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observarReserva: ObservarReservaUseCase,
    private val observarRestaurante: ObservarRestauranteUseCase,
    observarRecordatorios: ObservarRecordatoriosUseCase,
    private val observarOcupacion: ObservarOcupacionUseCase,
    private val modificarReserva: ModificarReservaUseCase,
) : ViewModel() {

    private val reservaId = savedStateHandle.toRoute<ModificarReserva>().reservaId
    private val eleccion = MutableStateFlow(EleccionCambio())
    private val reintento = MutableStateFlow(0)

    private val reloj = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(MILIS_POR_MINUTO)
        }
    }

    private val datos = reintento.flatMapLatest {
        observarReserva(reservaId)
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
            .flatMapLatest { resultado ->
                val reserva = resultado.getOrNull()
                val restaurante = reserva?.let { actual ->
                    observarRestaurante(actual.restauranteId).catch { emit(null) }
                } ?: flowOf(null)
                combine(restaurante, observarRecordatorios(), reloj) { local, recordatorios, ahora ->
                    DatosCambio(reserva, local, reserva?.let { recordatorios[it.id] }, ahora, resultado.isFailure)
                }
            }
    }

    private val fechaVista = combine(datos.map { it.reserva }, eleccion) { reserva, elegido ->
        reserva?.let { it.restauranteId to (elegido.fecha ?: it.fecha) }
    }.distinctUntilChanged()

    private val ocupacion = fechaVista.flatMapLatest { clave ->
        if (clave == null) flowOf(emptyMap())
        else observarOcupacion(clave.first, clave.second).catch { emit(emptyMap()) }.onStart { emit(emptyMap()) }
    }

    val uiState: StateFlow<ModificarReservaUiState> = combine(datos, eleccion, ocupacion) { datos, elegido, ocupados ->
        val reserva = datos.reserva
        val restaurante = datos.restaurante
        val fecha = elegido.fecha ?: reserva?.fecha
        val hora = if (elegido.fecha == null || elegido.fecha == reserva?.fecha) elegido.hora ?: reserva?.hora else elegido.hora
        val propios = if (reserva != null && fecha == reserva.fecha) {
            ocupados + (reserva.hora to ((ocupados[reserva.hora] ?: 0) - reserva.personas).coerceAtLeast(0))
        } else {
            ocupados
        }
        val horarios = if (reserva != null && restaurante != null && fecha != null) {
            horariosDelDia(restaurante.horario, fecha, datos.ahora, reserva.personas, propios, restaurante.cupoPorFranja)
        } else {
            emptyList()
        }
        ModificarReservaUiState(
            cargando = (reserva == null || restaurante == null) && !datos.sinConexion && elegido.guardado == null,
            sinConexion = datos.sinConexion,
            reserva = reserva,
            restaurante = restaurante,
            dias = restaurante?.let { diasReservables(it.horario, datos.ahora) }.orEmpty(),
            fecha = fecha,
            horarios = horarios,
            hora = hora,
            primeraHoraDeHoy = primeraHoraDeHoy(datos.ahora).takeIf { fecha == fechaLimaDe(datos.ahora) },
            recordatorio = datos.recordatorio,
            confirmando = elegido.confirmando,
            guardando = elegido.guardando,
            error = elegido.error,
            guardado = elegido.guardado,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ModificarReservaUiState())

    fun elegirFecha(fecha: String) = eleccion.update {
        if ((it.fecha ?: uiState.value.reserva?.fecha) == fecha) it else it.copy(fecha = fecha, hora = null)
    }

    fun elegirHora(hora: String) = eleccion.update { it.copy(hora = hora) }

    fun pedirConfirmacion() {
        if (uiState.value.puedeGuardar) eleccion.update { it.copy(confirmando = true) }
    }

    fun cerrarConfirmacion() = eleccion.update { it.copy(confirmando = false) }

    fun cerrarError() = eleccion.update { it.copy(error = null) }

    fun reintentar() = reintento.update { it + 1 }

    fun guardar() {
        val estado = uiState.value
        val reserva = estado.reserva ?: return
        val restaurante = estado.restaurante ?: return
        val fecha = estado.fecha ?: return
        val hora = estado.horaElegida ?: return
        eleccion.update { it.copy(confirmando = false, guardando = true) }
        viewModelScope.launch {
            try {
                val recordatorio = modificarReserva(reserva, fecha, hora, restaurante.horario, estado.recordatorio)
                eleccion.update {
                    it.copy(guardando = false, guardado = CambioGuardado(reserva, fecha, hora, recordatorio))
                }
            } catch (e: ErrorReservas) {
                eleccion.update {
                    it.copy(
                        guardando = false,
                        error = e,
                        hora = if (e == ErrorReservas.FranjaLlena || e == ErrorReservas.FueraDePlazo) null else it.hora,
                    )
                }
            }
        }
    }

    private companion object {
        const val MILIS_POR_MINUTO = 60_000L
    }
}
