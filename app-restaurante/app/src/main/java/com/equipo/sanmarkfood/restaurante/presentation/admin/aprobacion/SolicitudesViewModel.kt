package com.equipo.sanmarkfood.restaurante.presentation.admin.aprobacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.admin.ErrorAdmin
import com.equipo.sanmarkfood.restaurante.domain.model.admin.SolicitudLocal
import com.equipo.sanmarkfood.restaurante.domain.usecase.admin.ObservarSolicitudesPendientesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SolicitudesUiState(
    val cargando: Boolean = true,
    val solicitudes: List<SolicitudLocal> = emptyList(),
    val error: ErrorAdmin? = null,
)

@HiltViewModel
class SolicitudesViewModel @Inject constructor(
    private val observarSolicitudesPendientes: ObservarSolicitudesPendientesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SolicitudesUiState())
    val uiState: StateFlow<SolicitudesUiState> = _uiState.asStateFlow()

    private var observacion: Job? = null

    init {
        observar()
    }

    fun onReintentar() = observar()

    private fun observar() {
        observacion?.cancel()
        _uiState.update { it.copy(cargando = true, error = null) }
        observacion = viewModelScope.launch {
            observarSolicitudesPendientes()
                .catch { e -> if (e is ErrorAdmin) _uiState.update { it.copy(cargando = false, error = e) } else throw e }
                .collect { solicitudes ->
                    _uiState.update { it.copy(cargando = false, solicitudes = solicitudes, error = null) }
                }
        }
    }
}
