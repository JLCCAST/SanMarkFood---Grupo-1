package com.equipo.sanmarkfood.comensal.presentation.descubrimiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.ErrorDescubrimiento
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Restaurante
import com.equipo.sanmarkfood.comensal.domain.usecase.descubrimiento.ObtenerRestaurantesCercanosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DescubrimientoUiState(
    val isLoading: Boolean = true,
    val restaurantes: List<Restaurante> = emptyList(),
    val conDistancia: Boolean = false,
    val error: ErrorDescubrimiento? = null
)

@HiltViewModel
class DescubrimientoViewModel @Inject constructor(
    private val obtenerRestaurantesCercanos: ObtenerRestaurantesCercanosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DescubrimientoUiState())
    val uiState: StateFlow<DescubrimientoUiState> = _uiState.asStateFlow()

    private var cargaJob: Job? = null
    private var iniciado = false

    /** Primera carga de la pantalla; las siguientes llamadas no hacen nada. */
    fun iniciar(usarUbicacion: Boolean) {
        if (iniciado) return
        iniciado = true
        cargar(usarUbicacion)
    }

    /** Carga forzada: al reintentar o al conceder el permiso de ubicación. */
    fun cargar(usarUbicacion: Boolean) {
        cargaJob?.cancel()
        cargaJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            obtenerRestaurantesCercanos(usarUbicacion)
                .onSuccess { resultado ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            restaurantes = resultado.restaurantes,
                            conDistancia = resultado.conDistancia
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e as? ErrorDescubrimiento ?: ErrorDescubrimiento.Desconocido
                        )
                    }
                }
        }
    }
}