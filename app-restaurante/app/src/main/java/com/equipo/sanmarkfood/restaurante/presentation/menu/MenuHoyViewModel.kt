package com.equipo.sanmarkfood.restaurante.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.MenuDelDia
import com.equipo.sanmarkfood.restaurante.domain.model.menu.TipoOpcion
import com.equipo.sanmarkfood.restaurante.domain.model.menu.fechaDeHoy
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.CambiarDisponibilidadOpcionUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.CambiarHoraFinUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.ObservarMenuDeHoyUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.ObservarPizarraLeidaUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.ObtenerMenuDeAyerUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.ReabrirMenuDelDiaUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.TerminarMenuDelDiaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MenuHoyUiState(
    val fecha: String = fechaDeHoy(),
    val cargando: Boolean = true,
    val menu: MenuDelDia? = null,
    val error: ErrorMenu? = null,
    val menuDeAyer: MenuDelDia? = null,
    val guardando: Boolean = false,
    val errorGuardar: ErrorMenu? = null,
    val confirmandoTerminar: Boolean = false,
    val pizarraLeida: Boolean = false,
)

@HiltViewModel
class MenuHoyViewModel @Inject constructor(
    private val observarMenuDeHoy: ObservarMenuDeHoyUseCase,
    private val cambiarDisponibilidadOpcion: CambiarDisponibilidadOpcionUseCase,
    private val obtenerMenuDeAyer: ObtenerMenuDeAyerUseCase,
    private val cambiarHoraFin: CambiarHoraFinUseCase,
    private val terminarMenuDelDia: TerminarMenuDelDiaUseCase,
    private val reabrirMenuDelDia: ReabrirMenuDelDiaUseCase,
    private val observarPizarraLeida: ObservarPizarraLeidaUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuHoyUiState())
    val uiState: StateFlow<MenuHoyUiState> = _uiState.asStateFlow()

    private var observacion: Job? = null

    init {
        observar()
        cargarMenuDeAyer()
        viewModelScope.launch {
            observarPizarraLeida().collect { leida -> _uiState.update { it.copy(pizarraLeida = leida) } }
        }
    }

    fun onReintentar() = observar()

    fun onCambiarDisponible(tipo: TipoOpcion, indice: Int, disponible: Boolean) =
        guardar { menu -> cambiarDisponibilidadOpcion(menu, tipo, indice, disponible) }

    fun onElegirHoraFin(hora: String) = guardar { menu -> cambiarHoraFin(menu, hora) }

    fun onTerminar() = _uiState.update { it.copy(confirmandoTerminar = true) }

    fun onCancelarTerminar() = _uiState.update { it.copy(confirmandoTerminar = false) }

    fun onConfirmarTerminar() {
        _uiState.update { it.copy(confirmandoTerminar = false) }
        guardar { menu -> terminarMenuDelDia(menu) }
    }

    fun onReabrir() = guardar { menu -> reabrirMenuDelDia(menu) }

    private fun guardar(cambio: suspend (MenuDelDia) -> Unit) {
        val estado = _uiState.value
        val menu = estado.menu ?: return
        if (estado.guardando) return

        _uiState.update { it.copy(guardando = true, errorGuardar = null) }
        viewModelScope.launch {
            try {
                cambio(menu)
            } catch (e: ErrorMenu) {
                _uiState.update { it.copy(errorGuardar = e) }
            }
            _uiState.update { it.copy(guardando = false) }
        }
    }

    private fun cargarMenuDeAyer() {
        viewModelScope.launch {
            try {
                val menuDeAyer = obtenerMenuDeAyer()
                _uiState.update { it.copy(menuDeAyer = menuDeAyer) }
            } catch (e: ErrorMenu) {
            }
        }
    }

    private fun observar() {
        observacion?.cancel()
        _uiState.update { it.copy(cargando = true, error = null) }
        observacion = viewModelScope.launch {
            observarMenuDeHoy()
                .catch { e -> if (e is ErrorMenu) _uiState.update { it.copy(cargando = false, error = e) } else throw e }
                .collect { menu -> _uiState.update { it.copy(cargando = false, menu = menu, error = null) } }
        }
    }
}
