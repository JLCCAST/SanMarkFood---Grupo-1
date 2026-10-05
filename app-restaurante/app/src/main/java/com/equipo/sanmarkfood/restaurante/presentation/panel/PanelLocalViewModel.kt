package com.equipo.sanmarkfood.restaurante.presentation.panel

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Restaurante
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.usecase.pedidos.CambiarPausaPedidosUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.gestion_restaurante.ObservarRestauranteUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.ObservarCartaUseCase
import com.equipo.sanmarkfood.restaurante.presentation.menu.SeccionMenu
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
    val seccionMenu: SeccionMenu = SeccionMenu.HOY,
    val restaurante: Restaurante? = null,
    val error: ErrorRestaurante? = null,
    val cambiandoPausa: Boolean = false,
    val errorPausa: ErrorRestaurante? = null,
    val platosEnCarta: Int = 0,
)

@HiltViewModel
class PanelLocalViewModel @Inject constructor(
    private val observarRestaurante: ObservarRestauranteUseCase,
    private val cambiarPausaPedidos: CambiarPausaPedidosUseCase,
    private val observarCarta: ObservarCartaUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PanelLocalUiState())
    val uiState: StateFlow<PanelLocalUiState> = _uiState.asStateFlow()

    private var observacion: Job? = null

    init {
        observar()
        contarPlatos()
    }

    fun onElegirPestana(pestana: PestanaPanel) = _uiState.update { it.copy(pestana = pestana) }

    fun onElegirSeccionMenu(seccion: SeccionMenu) = _uiState.update { it.copy(seccionMenu = seccion) }

    fun onCargarCarta() = _uiState.update { it.copy(pestana = PestanaPanel.MENU, seccionMenu = SeccionMenu.CARTA) }

    fun onReintentar() = observar()

    // El interruptor no cambia por su cuenta: muestra lo que dice el documento, que se escucha en vivo.
    // Así, si la pausa no se guarda (sin conexión, por ejemplo), el interruptor no miente.
    fun onCambiarRecepcion(recibir: Boolean) {
        if (_uiState.value.cambiandoPausa) return

        _uiState.update { it.copy(cambiandoPausa = true, errorPausa = null) }
        viewModelScope.launch {
            try {
                cambiarPausaPedidos(pausado = !recibir)
                _uiState.update { it.copy(cambiandoPausa = false) }
            } catch (e: ErrorRestaurante) {
                _uiState.update { it.copy(cambiandoPausa = false, errorPausa = e) }
            }
        }
    }

    // El estado se escucha en vivo: si el administrador aprueba o rechaza el local, la pantalla cambia sola.
    private fun observar() {
        observacion?.cancel()
        _uiState.update { it.copy(error = null) }
        observacion = viewModelScope.launch {
            observarRestaurante()
                .catch { e -> if (e is ErrorRestaurante) _uiState.update { it.copy(error = e) } else throw e }
                .collect { restaurante ->
                    _uiState.update {
                        val volvioARevision = it.restaurante?.estado == EstadoRestaurante.APROBADO &&
                            restaurante?.estado == EstadoRestaurante.PENDIENTE
                        it.copy(
                            restaurante = restaurante,
                            pestana = if (volvioARevision) PestanaPanel.PEDIDOS else it.pestana,
                            error = if (restaurante == null) ErrorRestaurante.Desconocido else null,
                        )
                    }
                }
        }
    }

    private fun contarPlatos() {
        viewModelScope.launch {
            observarCarta()
                .catch { e -> if (e !is ErrorMenu) throw e }
                .collect { secciones -> _uiState.update { it.copy(platosEnCarta = secciones.sumOf { s -> s.platos.size }) } }
        }
    }
}
