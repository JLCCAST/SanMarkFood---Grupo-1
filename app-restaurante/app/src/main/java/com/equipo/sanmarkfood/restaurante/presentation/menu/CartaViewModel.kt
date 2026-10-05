package com.equipo.sanmarkfood.restaurante.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.Plato
import com.equipo.sanmarkfood.restaurante.domain.model.menu.SeccionCarta
import com.equipo.sanmarkfood.restaurante.domain.model.menu.fechaDeHoy
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.CambiarDisponibilidadPlatoUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.ObservarCartaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartaUiState(
    val hoy: String = fechaDeHoy(),
    val secciones: List<SeccionCarta>? = null,
    val error: ErrorMenu? = null,
    val cambiando: Set<String> = emptySet(),
    val errorDisponibilidad: ErrorMenu? = null,
)

@HiltViewModel
class CartaViewModel @Inject constructor(
    private val observarCarta: ObservarCartaUseCase,
    private val cambiarDisponibilidadPlato: CambiarDisponibilidadPlatoUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartaUiState())
    val uiState: StateFlow<CartaUiState> = _uiState.asStateFlow()

    private var observacion: Job? = null

    init {
        observar()
    }

    fun onReintentar() = observar()

    fun onCambiarDisponible(plato: Plato, disponible: Boolean) {
        if (plato.id in _uiState.value.cambiando) return

        _uiState.update { it.copy(cambiando = it.cambiando + plato.id, errorDisponibilidad = null) }
        viewModelScope.launch {
            try {
                cambiarDisponibilidadPlato(plato, disponible)
            } catch (e: ErrorMenu) {
                _uiState.update { it.copy(errorDisponibilidad = e) }
            }
            _uiState.update { it.copy(cambiando = it.cambiando - plato.id) }
        }
    }

    private fun observar() {
        observacion?.cancel()
        _uiState.update { it.copy(error = null) }
        observacion = viewModelScope.launch {
            observarCarta()
                .catch { e -> if (e is ErrorMenu) _uiState.update { it.copy(error = e) } else throw e }
                .collect { secciones -> _uiState.update { it.copy(secciones = secciones, error = null) } }
        }
    }
}
