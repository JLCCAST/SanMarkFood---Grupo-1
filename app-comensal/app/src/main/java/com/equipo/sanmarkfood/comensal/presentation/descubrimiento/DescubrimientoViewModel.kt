package com.equipo.sanmarkfood.comensal.presentation.descubrimiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.ErrorDescubrimiento
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.FiltroDescubrimiento
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Restaurante
import com.equipo.sanmarkfood.comensal.domain.usecase.descubrimiento.FiltrarRestaurantesUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.descubrimiento.ObtenerRestaurantesCercanosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DescubrimientoUiState(
    val isLoading: Boolean = true,
    val restaurantes: List<Restaurante> = emptyList(),
    val conDistancia: Boolean = false,
    val filtro: FiltroDescubrimiento = FiltroDescubrimiento(),
    val error: ErrorDescubrimiento? = null
)

@HiltViewModel
class DescubrimientoViewModel @Inject constructor(
    private val obtenerRestaurantesCercanos: ObtenerRestaurantesCercanosUseCase,
    private val filtrarRestaurantes: FiltrarRestaurantesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DescubrimientoUiState())
    val uiState: StateFlow<DescubrimientoUiState> = _uiState.asStateFlow()

    // Escucha activa de la lista en Firestore; se reinicia al reintentar o al cambiar
    // el permiso de ubicación, y se cancela sola cuando el ViewModel se destruye.
    private var escuchaJob: Job? = null
    private var iniciado = false

    // Lista cruda de Firestore, sin filtrar; filtrar() la vuelve a filtrar sin volver a consultar.
    private var restaurantesSinFiltrar: List<Restaurante> = emptyList()

    /** Primera carga de la pantalla; las siguientes llamadas no hacen nada. */
    fun iniciar(usarUbicacion: Boolean) {
        if (iniciado) return
        iniciado = true
        cargar(usarUbicacion)
    }

    /** Reinicia la escucha: al reintentar o al conceder el permiso de ubicación. */
    fun cargar(usarUbicacion: Boolean) {
        escuchaJob?.cancel()
        escuchaJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            obtenerRestaurantesCercanos.observar(usarUbicacion)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e as? ErrorDescubrimiento ?: ErrorDescubrimiento.Desconocido
                        )
                    }
                }
                .collect { resultado ->
                    restaurantesSinFiltrar = resultado.restaurantes
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            restaurantes = filtrarRestaurantes(resultado.restaurantes, it.filtro),
                            conDistancia = resultado.conDistancia,
                            error = null
                        )
                    }
                }
        }
    }

    /** La llama la pantalla de filtros cada vez que cambia un criterio; no toca Firestore. */
    fun filtrar(nuevoFiltro: FiltroDescubrimiento) {
        _uiState.update {
            it.copy(filtro = nuevoFiltro, restaurantes = filtrarRestaurantes(restaurantesSinFiltrar, nuevoFiltro))
        }
    }
}