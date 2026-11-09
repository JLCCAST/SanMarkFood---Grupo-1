package com.equipo.sanmarkfood.comensal.presentation.pedidos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.equipo.sanmarkfood.comensal.core.navigation.DetalleRestaurante
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Carrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ErrorPedidos
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ItemCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Plato
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ResultadoAgregarAlCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.SeccionCarta
import com.equipo.sanmarkfood.comensal.domain.usecase.pedidos.AgregarAlCarritoUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.pedidos.CambiarCantidadCarritoUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.pedidos.ObservarCarritoUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.pedidos.ObservarCartaUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.pedidos.QuitarDelCarritoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlatoPendiente(
    val plato: Plato,
    val restauranteNombre: String,
    val restauranteActual: String,
)

data class CartaUiState(
    val cargando: Boolean = true,
    val secciones: List<SeccionCarta> = emptyList(),
    val carrito: Carrito? = null,
    val error: ErrorPedidos? = null,
    val platoPendiente: PlatoPendiente? = null,
    val limiteAlcanzado: Boolean = false,
)

@HiltViewModel
class CartaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observarCarta: ObservarCartaUseCase,
    observarCarrito: ObservarCarritoUseCase,
    private val agregarAlCarrito: AgregarAlCarritoUseCase,
    private val cambiarCantidadCarrito: CambiarCantidadCarritoUseCase,
    private val quitarDelCarrito: QuitarDelCarritoUseCase,
) : ViewModel() {

    private val restauranteId = savedStateHandle.toRoute<DetalleRestaurante>().restauranteId

    private val _uiState = MutableStateFlow(CartaUiState())
    val uiState: StateFlow<CartaUiState> = _uiState.asStateFlow()

    private var cartaJob: Job? = null

    init {
        escucharCarta()
        viewModelScope.launch {
            observarCarrito().collect { carrito -> _uiState.update { it.copy(carrito = carrito) } }
        }
    }

    fun reintentar() {
        _uiState.update { it.copy(cargando = true, error = null) }
        escucharCarta()
    }

    fun agregar(plato: Plato, restauranteNombre: String) {
        if (plato.agotadoHoy) return
        viewModelScope.launch { resolver(plato, restauranteNombre, vaciarSiOtroLocal = false) }
    }

    fun vaciarYAgregar() {
        val pendiente = _uiState.value.platoPendiente ?: return
        _uiState.update { it.copy(platoPendiente = null) }
        viewModelScope.launch { resolver(pendiente.plato, pendiente.restauranteNombre, vaciarSiOtroLocal = true) }
    }

    fun mantenerActual() {
        _uiState.update { it.copy(platoPendiente = null) }
    }

    fun limpiarLimite() {
        _uiState.update { it.copy(limiteAlcanzado = false) }
    }

    fun cambiarCantidad(platoId: String, cantidad: Int) {
        viewModelScope.launch { cambiarCantidadCarrito(platoId, cantidad) }
    }

    fun quitar(platoId: String) {
        viewModelScope.launch { quitarDelCarrito(platoId) }
    }

    private fun escucharCarta() {
        cartaJob?.cancel()
        cartaJob = viewModelScope.launch {
            observarCarta(restauranteId)
                .catch { e ->
                    _uiState.update {
                        it.copy(cargando = false, error = e as? ErrorPedidos ?: ErrorPedidos.Desconocido)
                    }
                }
                .collect { secciones ->
                    _uiState.update { it.copy(cargando = false, secciones = secciones, error = null) }
                }
        }
    }

    private suspend fun resolver(plato: Plato, restauranteNombre: String, vaciarSiOtroLocal: Boolean) {
        val item = ItemCarrito(
            platoId = plato.id,
            nombre = plato.nombre,
            precio = plato.precio,
            cantidad = 1,
            fotoUrl = plato.fotoUrl,
        )
        when (val resultado = agregarAlCarrito(restauranteId, restauranteNombre, item, vaciarSiOtroLocal)) {
            ResultadoAgregarAlCarrito.Agregado -> Unit
            is ResultadoAgregarAlCarrito.OtroLocal -> _uiState.update {
                it.copy(platoPendiente = PlatoPendiente(plato, restauranteNombre, resultado.restauranteActual))
            }
            ResultadoAgregarAlCarrito.LimiteAlcanzado -> _uiState.update { it.copy(limiteAlcanzado = true) }
        }
    }
}