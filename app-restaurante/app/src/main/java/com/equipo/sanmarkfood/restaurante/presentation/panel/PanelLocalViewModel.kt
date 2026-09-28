package com.equipo.sanmarkfood.restaurante.presentation.panel

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.Restaurante
import com.equipo.sanmarkfood.restaurante.domain.usecase.ObservarRestauranteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PestanaPanel(@param:DrawableRes val icono: Int, @param:StringRes val etiqueta: Int) {
    PEDIDOS(R.drawable.ic_pedidos, R.string.panel_pedidos),
    RESERVAS(R.drawable.ic_reservas, R.string.panel_reservas),
    MENU(R.drawable.ic_menu, R.string.panel_menu),
    RESENAS(R.drawable.ic_resenas, R.string.panel_resenas),
    NEGOCIO(R.drawable.ic_negocio, R.string.panel_negocio),
}

/** Mientras [restaurante] y [error] son null, se está leyendo el local por primera vez. */
data class PanelLocalUiState(
    val pestana: PestanaPanel = PestanaPanel.PEDIDOS,
    val restaurante: Restaurante? = null,
    val error: ErrorRestaurante? = null,
)

@HiltViewModel
class PanelLocalViewModel @Inject constructor(
    private val observarRestaurante: ObservarRestauranteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PanelLocalUiState())
    val uiState: StateFlow<PanelLocalUiState> = _uiState.asStateFlow()

    private var observacion: Job? = null

    init {
        observar()
    }

    fun onElegirPestana(pestana: PestanaPanel) = _uiState.update { it.copy(pestana = pestana) }

    fun onReintentar() = observar()

    // El estado se escucha en vivo: si el administrador aprueba o rechaza el local, la pantalla cambia sola.
    private fun observar() {
        observacion?.cancel()
        _uiState.update { it.copy(error = null) }
        observacion = viewModelScope.launch {
            observarRestaurante()
                .catch { e -> if (e is ErrorRestaurante) _uiState.update { it.copy(error = e) } else throw e }
                .collect { restaurante ->
                    _uiState.update {
                        it.copy(
                            restaurante = restaurante,
                            error = if (restaurante == null) ErrorRestaurante.Desconocido else null,
                        )
                    }
                }
        }
    }
}
