package com.equipo.sanmarkfood.restaurante.presentation.admin.aprobacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.admin.ErrorAdmin
import com.equipo.sanmarkfood.restaurante.domain.model.admin.FiltroSolicitudes
import com.equipo.sanmarkfood.restaurante.domain.model.admin.SolicitudLocal
import com.equipo.sanmarkfood.restaurante.domain.usecase.admin.ObservarSolicitudesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListaSolicitudes(
    val cargando: Boolean = true,
    val solicitudes: List<SolicitudLocal> = emptyList(),
    val error: ErrorAdmin? = null,
)

data class SolicitudesUiState(
    val filtro: FiltroSolicitudes = FiltroSolicitudes.PENDIENTES,
    val pendientes: ListaSolicitudes = ListaSolicitudes(),
    val revisadas: ListaSolicitudes = ListaSolicitudes(),
) {
    val lista: ListaSolicitudes
        get() = if (filtro == FiltroSolicitudes.PENDIENTES) pendientes else revisadas

    val cantidadPendientes: Int?
        get() = pendientes.solicitudes.size.takeUnless { pendientes.cargando || pendientes.error != null }
}

@HiltViewModel
class SolicitudesViewModel @Inject constructor(
    private val observarSolicitudes: ObservarSolicitudesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SolicitudesUiState())
    val uiState: StateFlow<SolicitudesUiState> = _uiState.asStateFlow()

    private var observacionPendientes: Job? = null
    private var observacionRevisadas: Job? = null

    init {
        observarPendientes()
    }

    fun onElegirFiltro(filtro: FiltroSolicitudes) {
        if (filtro == _uiState.value.filtro) return
        _uiState.update { it.copy(filtro = filtro) }
        if (filtro == FiltroSolicitudes.PENDIENTES) {
            observacionRevisadas?.cancel()
            _uiState.update { it.copy(revisadas = ListaSolicitudes()) }
        } else {
            observarRevisadas(filtro)
        }
    }

    fun onReintentar() {
        val filtro = _uiState.value.filtro
        if (filtro == FiltroSolicitudes.PENDIENTES) observarPendientes() else observarRevisadas(filtro)
    }

    private fun observarPendientes() {
        observacionPendientes?.cancel()
        _uiState.update { it.copy(pendientes = it.pendientes.copy(cargando = true, error = null)) }
        observacionPendientes = escuchar(FiltroSolicitudes.PENDIENTES) { cambio ->
            _uiState.update { it.copy(pendientes = cambio(it.pendientes)) }
        }
    }

    private fun observarRevisadas(filtro: FiltroSolicitudes) {
        observacionRevisadas?.cancel()
        _uiState.update { it.copy(revisadas = ListaSolicitudes()) }
        observacionRevisadas = escuchar(filtro) { cambio ->
            _uiState.update { it.copy(revisadas = cambio(it.revisadas)) }
        }
    }

    private fun escuchar(
        filtro: FiltroSolicitudes,
        actualizar: ((ListaSolicitudes) -> ListaSolicitudes) -> Unit,
    ): Job = viewModelScope.launch {
        observarSolicitudes(filtro)
            .catch { e -> if (e is ErrorAdmin) actualizar { it.copy(cargando = false, error = e) } else throw e }
            .collect { solicitudes -> actualizar { ListaSolicitudes(cargando = false, solicitudes = solicitudes) } }
    }
}
