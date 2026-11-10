package com.equipo.sanmarkfood.comensal.presentation.pedidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Carrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MetodoPago
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Modalidad
import com.equipo.sanmarkfood.comensal.domain.usecase.pedidos.CambiarCantidadCarritoUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.pedidos.ObservarCarritoUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.pedidos.QuitarDelCarritoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TuPedidoUiState(
    val cargando: Boolean = true,
    val carrito: Carrito? = null,
    val modalidad: Modalidad = Modalidad.RECOJO,
    val metodoPago: MetodoPago = MetodoPago.BILLETERA,
)

@HiltViewModel
class TuPedidoViewModel @Inject constructor(
    observarCarrito: ObservarCarritoUseCase,
    private val cambiarCantidadCarrito: CambiarCantidadCarritoUseCase,
    private val quitarDelCarrito: QuitarDelCarritoUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TuPedidoUiState())
    val uiState: StateFlow<TuPedidoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observarCarrito().collect { carrito ->
                _uiState.update { it.copy(cargando = false, carrito = carrito) }
            }
        }
    }

    fun cambiarModalidad(modalidad: Modalidad) {
        _uiState.update { it.copy(modalidad = modalidad) }
    }

    fun cambiarMetodoPago(metodo: MetodoPago) {
        _uiState.update { it.copy(metodoPago = metodo) }
    }

    fun cambiarCantidad(clave: String, cantidad: Int) {
        viewModelScope.launch { cambiarCantidadCarrito(clave, cantidad) }
    }

    fun quitar(clave: String) {
        viewModelScope.launch { quitarDelCarrito(clave) }
    }
}