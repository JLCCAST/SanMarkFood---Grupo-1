package com.equipo.sanmarkfood.comensal.presentation.reservas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.equipo.sanmarkfood.comensal.core.navigation.ReservarMesa
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Restaurante
import com.equipo.sanmarkfood.comensal.domain.model.reservas.DiaReservable
import com.equipo.sanmarkfood.comensal.domain.model.reservas.ErrorReservas
import com.equipo.sanmarkfood.comensal.domain.model.reservas.HorarioReservable
import com.equipo.sanmarkfood.comensal.domain.model.reservas.MAX_COMENSALES
import com.equipo.sanmarkfood.comensal.domain.model.reservas.MIN_COMENSALES
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.alcanzoElTope
import com.equipo.sanmarkfood.comensal.domain.model.reservas.diasReservables
import com.equipo.sanmarkfood.comensal.domain.model.reservas.fechaLimaDe
import com.equipo.sanmarkfood.comensal.domain.model.reservas.horariosDelDia
import com.equipo.sanmarkfood.comensal.domain.model.reservas.primeraHoraDeHoy
import com.equipo.sanmarkfood.comensal.domain.usecase.descubrimiento.ObservarRestauranteUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.reservas.ObservarOcupacionUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.reservas.ObservarReservasUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.reservas.SolicitarReservaUseCase
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

data class ReservaEnviada(
    val reservaId: String,
    val restauranteNombre: String,
    val fecha: String,
    val hora: String,
    val personas: Int,
)

data class ReservarMesaUiState(
    val cargando: Boolean = true,
    val sinConexion: Boolean = false,
    val restaurante: Restaurante? = null,
    val personas: Int = 2,
    val dias: List<DiaReservable> = emptyList(),
    val fecha: String? = null,
    val horarios: List<HorarioReservable> = emptyList(),
    val hora: String? = null,
    val primeraHoraDeHoy: String? = null,
    val reservas: List<Reserva> = emptyList(),
    val alcanzoElTope: Boolean = false,
    val enviando: Boolean = false,
    val error: ErrorReservas? = null,
    val enviada: ReservaEnviada? = null,
) {
    val horaElegida: String?
        get() = hora?.takeIf { elegida -> horarios.any { it.hora == elegida && !it.lleno } }

    val puedeSolicitar: Boolean
        get() = restaurante?.aceptaReservas == true && !alcanzoElTope && fecha != null &&
                horaElegida != null && !enviando
}

private data class Seleccion(
    val personas: Int = 2,
    val fecha: String? = null,
    val hora: String? = null,
    val enviando: Boolean = false,
    val error: ErrorReservas? = null,
    val enviada: ReservaEnviada? = null,
)

private data class Datos(
    val restaurante: Restaurante?,
    val reservas: List<Reserva>,
    val ahora: Long,
    val sinConexion: Boolean,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReservarMesaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observarRestaurante: ObservarRestauranteUseCase,
    observarReservas: ObservarReservasUseCase,
    private val observarOcupacion: ObservarOcupacionUseCase,
    private val solicitarReserva: SolicitarReservaUseCase,
) : ViewModel() {

    private val restauranteId = savedStateHandle.toRoute<ReservarMesa>().restauranteId
    private val seleccion = MutableStateFlow(Seleccion())
    private val reintento = MutableStateFlow(0)

    private val reloj = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(MILIS_POR_MINUTO)
        }
    }

    private val datos = reintento.flatMapLatest {
        combine(
            observarRestaurante(restauranteId).map { Result.success(it) }.catch { emit(Result.failure(it)) },
            observarReservas().catch { emit(emptyList()) },
            reloj,
        ) { restaurante, reservas, ahora ->
            Datos(restaurante.getOrNull(), reservas, ahora, restaurante.isFailure)
        }
    }

    private val ocupacion = seleccion.map { it.fecha }.distinctUntilChanged().flatMapLatest { fecha ->
        if (fecha == null) flowOf(emptyMap())
        else observarOcupacion(restauranteId, fecha).catch { emit(emptyMap()) }.onStart { emit(emptyMap()) }
    }

    val uiState: StateFlow<ReservarMesaUiState> = combine(datos, seleccion, ocupacion) { datos, eleccion, ocupados ->
        val restaurante = datos.restaurante
        val dias = restaurante?.let { diasReservables(it.horario, datos.ahora) }.orEmpty()
        val fecha = eleccion.fecha?.takeIf { elegida -> dias.any { it.fecha == elegida && it.disponible } }
            ?: dias.firstOrNull { it.disponible }?.fecha
        val horarios = if (restaurante != null && fecha != null) {
            horariosDelDia(restaurante.horario, fecha, datos.ahora, eleccion.personas, ocupados, restaurante.cupoPorFranja)
        } else {
            emptyList()
        }
        ReservarMesaUiState(
            cargando = restaurante == null && !datos.sinConexion,
            sinConexion = datos.sinConexion,
            restaurante = restaurante,
            personas = eleccion.personas,
            dias = dias,
            fecha = fecha,
            horarios = horarios,
            hora = eleccion.hora,
            primeraHoraDeHoy = primeraHoraDeHoy(datos.ahora).takeIf { fecha == fechaLimaDe(datos.ahora) },
            reservas = datos.reservas,
            alcanzoElTope = datos.reservas.alcanzoElTope(fechaLimaDe(datos.ahora)),
            enviando = eleccion.enviando,
            error = eleccion.error,
            enviada = eleccion.enviada,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReservarMesaUiState())

    fun menosComensales() = seleccion.update { it.copy(personas = (it.personas - 1).coerceAtLeast(MIN_COMENSALES)) }

    fun masComensales() = seleccion.update { it.copy(personas = (it.personas + 1).coerceAtMost(MAX_COMENSALES)) }

    fun elegirFecha(fecha: String) = seleccion.update {
        if (it.fecha == fecha) it else it.copy(fecha = fecha, hora = null)
    }

    fun elegirHora(hora: String) = seleccion.update { it.copy(hora = hora) }

    fun reintentarCarga() = reintento.update { it + 1 }

    fun cerrarError() = seleccion.update { it.copy(error = null) }

    fun solicitar() {
        val estado = uiState.value
        val restaurante = estado.restaurante ?: return
        val fecha = estado.fecha ?: return
        val hora = estado.horaElegida ?: return
        if (!estado.puedeSolicitar) return
        seleccion.update { it.copy(enviando = true, error = null) }
        viewModelScope.launch {
            try {
                val reservaId = solicitarReserva(restaurante, fecha, hora, estado.personas, estado.reservas)
                seleccion.update {
                    it.copy(
                        enviando = false,
                        enviada = ReservaEnviada(reservaId, restaurante.nombre, fecha, hora, estado.personas),
                    )
                }
            } catch (e: ErrorReservas) {
                seleccion.update {
                    it.copy(
                        enviando = false,
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
