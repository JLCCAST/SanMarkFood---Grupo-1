package com.equipo.sanmarkfood.restaurante.presentation.pedidos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.ErrorPedidos
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.restaurante.domain.usecase.pedidos.AvanzarPedidoUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.pedidos.EntregarPedidoUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.pedidos.ObservarPedidosDeHoyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.equipo.sanmarkfood.restaurante.core.navigation.PedidoDetalle as RutaPedidoDetalle

data class PedidoDetalleUiState(
    val cargando: Boolean = true,
    val pedido: Pedido? = null,
    val enviando: Boolean = false,
    val confirmandoSinCodigo: Boolean = false,
    val error: ErrorPedidos? = null,
)

@HiltViewModel
class PedidoDetalleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observarPedidosDeHoy: ObservarPedidosDeHoyUseCase,
    private val avanzarPedido: AvanzarPedidoUseCase,
    private val entregarPedido: EntregarPedidoUseCase,
) : ViewModel() {

    private val pedidoId: String = savedStateHandle.toRoute<RutaPedidoDetalle>().pedidoId

    private val _uiState = MutableStateFlow(PedidoDetalleUiState())
    val uiState: StateFlow<PedidoDetalleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observarPedidosDeHoy()
                .catch { e -> if (e is ErrorPedidos) _uiState.update { it.copy(cargando = false, error = e) } else throw e }
                .collect { deHoy ->
                    _uiState.update { it.copy(cargando = false, pedido = deHoy.pedidos.firstOrNull { pedido -> pedido.id == pedidoId }) }
                }
        }
    }

    fun onAvanzar() = enviar { avanzarPedido(it) }

    fun onEntregar() = enviar { entregarPedido(it) }

    fun onPedirSinCodigo() = _uiState.update { it.copy(confirmandoSinCodigo = true) }

    fun onCancelarSinCodigo() = _uiState.update { it.copy(confirmandoSinCodigo = false) }

    fun onConfirmarSinCodigo() {
        _uiState.update { it.copy(confirmandoSinCodigo = false) }
        enviar { entregarPedido(it) }
    }

    private fun enviar(accion: suspend (Pedido) -> Unit) {
        val estado = _uiState.value
        val pedido = estado.pedido ?: return
        if (estado.enviando) return

        _uiState.update { it.copy(enviando = true, error = null) }
        viewModelScope.launch {
            try {
                accion(pedido)
                _uiState.update { it.copy(enviando = false) }
            } catch (e: ErrorPedidos) {
                _uiState.update { it.copy(enviando = false, error = e) }
            }
        }
    }
}
