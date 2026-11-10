package com.equipo.sanmarkfood.comensal.presentation.pedidos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.equipo.sanmarkfood.comensal.core.navigation.SeguimientoPedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ErrorCancelarPedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ErrorPedidos
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.EstadoPedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.comensal.domain.usecase.pedidos.CancelarPedidoUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.pedidos.ObservarPedidoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeguimientoPedidoUiState(
    val cargando: Boolean = true,
    val pedido: Pedido? = null,
    val error: ErrorPedidos? = null,
    val confirmandoCancelacion: Boolean = false,
    val cancelando: Boolean = false,
    val errorCancelar: ErrorCancelarPedido? = null,
) {
    val puedeCancelar: Boolean
        get() = pedido?.estado == EstadoPedido.RECIBIDO && !cancelando
}

@HiltViewModel
class SeguimientoPedidoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observarPedido: ObservarPedidoUseCase,
    private val cancelarPedido: CancelarPedidoUseCase,
) : ViewModel() {

    private val pedidoId = savedStateHandle.toRoute<SeguimientoPedido>().pedidoId

    private val _uiState = MutableStateFlow(SeguimientoPedidoUiState())
    val uiState: StateFlow<SeguimientoPedidoUiState> = _uiState.asStateFlow()

    private var pedidoJob: Job? = null

    init {
        escucharPedido()
    }

    fun reintentar() {
        _uiState.update { it.copy(cargando = true, error = null) }
        escucharPedido()
    }

    fun pedirCancelacion() {
        if (!_uiState.value.puedeCancelar) return
        _uiState.update { it.copy(confirmandoCancelacion = true) }
    }

    fun mantenerPedido() {
        _uiState.update { it.copy(confirmandoCancelacion = false) }
    }

    fun cancelar() {
        val pedido = _uiState.value.pedido ?: return
        _uiState.update { it.copy(confirmandoCancelacion = false, cancelando = true, errorCancelar = null) }
        viewModelScope.launch {
            try {
                cancelarPedido(pedido)
                _uiState.update { it.copy(cancelando = false) }
            } catch (e: ErrorCancelarPedido) {
                _uiState.update { it.copy(cancelando = false, errorCancelar = e) }
            }
        }
    }

    fun errorCancelarMostrado() {
        _uiState.update { it.copy(errorCancelar = null) }
    }

    private fun escucharPedido() {
        pedidoJob?.cancel()
        pedidoJob = viewModelScope.launch {
            observarPedido(pedidoId)
                .catch { e ->
                    _uiState.update {
                        it.copy(cargando = false, error = e as? ErrorPedidos ?: ErrorPedidos.Desconocido)
                    }
                }
                .collect { pedido ->
                    _uiState.update { it.copy(cargando = false, pedido = pedido, error = null) }
                }
        }
    }
}
