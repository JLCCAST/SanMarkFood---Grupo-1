package com.equipo.sanmarkfood.restaurante.presentation.admin.aprobacion

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.equipo.sanmarkfood.restaurante.core.navigation.RevisarSolicitud
import com.equipo.sanmarkfood.restaurante.domain.model.admin.DetalleSolicitud
import com.equipo.sanmarkfood.restaurante.domain.model.admin.ErrorAdmin
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.MotivoRechazo
import com.equipo.sanmarkfood.restaurante.domain.usecase.admin.AprobarLocalUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.admin.ObservarDetalleSolicitudUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.admin.RechazarLocalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RevisarSolicitudUiState(
    val cargando: Boolean = true,
    val solicitud: DetalleSolicitud? = null,
    val errorCarga: ErrorAdmin? = null,
    val aprobando: Boolean = false,
    val errorDecision: ErrorAdmin? = null,
    val hojaRechazo: HojaRechazoUiState? = null,
)

data class HojaRechazoUiState(
    val motivos: Set<MotivoRechazo> = emptySet(),
    val detalle: String = "",
    val enviando: Boolean = false,
    val error: ErrorAdmin? = null,
) {
    val completa: Boolean
        get() = motivos.isNotEmpty() &&
            (MotivoRechazo.OTRO !in motivos || detalle.trim().length >= RechazarLocalUseCase.MIN_DETALLE_OTRO)
}

@HiltViewModel
class RevisarSolicitudViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observarDetalleSolicitud: ObservarDetalleSolicitudUseCase,
    private val aprobarLocal: AprobarLocalUseCase,
    private val rechazarLocal: RechazarLocalUseCase,
) : ViewModel() {

    private val uid = savedStateHandle.toRoute<RevisarSolicitud>().uid

    private val _uiState = MutableStateFlow(RevisarSolicitudUiState())
    val uiState: StateFlow<RevisarSolicitudUiState> = _uiState.asStateFlow()

    private var observacion: Job? = null

    init {
        observar()
    }

    fun onReintentar() = observar()

    fun onAprobar() {
        val estado = _uiState.value
        if (estado.aprobando || estado.hojaRechazo != null) return

        _uiState.update { it.copy(aprobando = true, errorDecision = null) }
        viewModelScope.launch {
            try {
                aprobarLocal(uid)
                _uiState.update { it.copy(aprobando = false) }
            } catch (e: ErrorAdmin) {
                _uiState.update { it.copy(aprobando = false, errorDecision = e) }
            }
        }
    }

    fun onAbrirRechazo() {
        if (_uiState.value.aprobando) return
        _uiState.update { it.copy(hojaRechazo = HojaRechazoUiState(), errorDecision = null) }
    }

    fun onCerrarRechazo() = _uiState.update { it.copy(hojaRechazo = null) }

    fun onAlternarMotivo(motivo: MotivoRechazo) = actualizarHoja {
        it.copy(motivos = if (motivo in it.motivos) it.motivos - motivo else it.motivos + motivo, error = null)
    }

    fun onCambiarDetalle(detalle: String) =
        actualizarHoja { it.copy(detalle = detalle.take(RechazarLocalUseCase.MAX_DETALLE), error = null) }

    fun onRechazar() {
        val hoja = _uiState.value.hojaRechazo ?: return
        if (hoja.enviando) return

        actualizarHoja { it.copy(enviando = true, error = null) }
        viewModelScope.launch {
            try {
                rechazarLocal(uid, hoja.motivos, hoja.detalle)
                _uiState.update { it.copy(hojaRechazo = null) }
            } catch (e: ErrorAdmin) {
                _uiState.update { estado ->
                    val abierta = estado.hojaRechazo
                    if (abierta != null) estado.copy(hojaRechazo = abierta.copy(enviando = false, error = e))
                    else estado.copy(errorDecision = e)
                }
            }
        }
    }

    private fun actualizarHoja(cambio: (HojaRechazoUiState) -> HojaRechazoUiState) = _uiState.update { estado ->
        estado.hojaRechazo?.let { estado.copy(hojaRechazo = cambio(it)) } ?: estado
    }

    private fun observar() {
        observacion?.cancel()
        _uiState.update { it.copy(cargando = true, errorCarga = null) }
        observacion = viewModelScope.launch {
            observarDetalleSolicitud(uid)
                .catch { e -> if (e is ErrorAdmin) _uiState.update { it.copy(cargando = false, errorCarga = e) } else throw e }
                .collect { solicitud ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            solicitud = solicitud,
                            errorCarga = if (solicitud == null) ErrorAdmin.Desconocido else null,
                        )
                    }
                }
        }
    }
}
