package com.equipo.sanmarkfood.restaurante.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.MenuDelDia
import com.equipo.sanmarkfood.restaurante.domain.model.menu.TipoOpcion
import com.equipo.sanmarkfood.restaurante.domain.model.menu.fechaDeHoy
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.CambiarDisponibilidadOpcionUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.ObservarMenuDeHoyUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.ObtenerMenuDeAyerUseCase
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
    val cambiandoOpcion: Boolean = false,
    val errorOpcion: ErrorMenu? = null,
)

@HiltViewModel
class MenuHoyViewModel @Inject constructor(
    private val observarMenuDeHoy: ObservarMenuDeHoyUseCase,
    private val cambiarDisponibilidadOpcion: CambiarDisponibilidadOpcionUseCase,
    private val obtenerMenuDeAyer: ObtenerMenuDeAyerUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuHoyUiState())
    val uiState: StateFlow<MenuHoyUiState> = _uiState.asStateFlow()

    private var observacion: Job? = null

    init {
        observar()
        cargarMenuDeAyer()
    }

    fun onReintentar() = observar()

    fun onCambiarDisponible(tipo: TipoOpcion, indice: Int, disponible: Boolean) {
        val estado = _uiState.value
        val menu = estado.menu ?: return
        if (estado.cambiandoOpcion) return

        _uiState.update { it.copy(cambiandoOpcion = true, errorOpcion = null) }
        viewModelScope.launch {
            try {
                cambiarDisponibilidadOpcion(menu, tipo, indice, disponible)
            } catch (e: ErrorMenu) {
                _uiState.update { it.copy(errorOpcion = e) }
            }
            _uiState.update { it.copy(cambiandoOpcion = false) }
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
