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
import com.equipo.sanmarkfood.comensal.domain.model.resenas.ErrorCalificar
import com.equipo.sanmarkfood.comensal.domain.usecase.pedidos.CancelarPedidoUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.pedidos.ObservarPedidoUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.pedidos.RecuperarPedidoEnCarritoUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.resenas.CalificarPedidoUseCase
import com.equipo.sanmarkfood.comensal.presentation.resenas.CalificacionUiState
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
    val irALaCarta: String? = null,
    val calificacion: CalificacionUiState = CalificacionUiState(),
) {
    val puedeCancelar: Boolean
        get() = pedido?.estado == EstadoPedido.RECIBIDO && !cancelando
}

@HiltViewModel
class SeguimientoPedidoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observarPedido: ObservarPedidoUseCase,
    private val cancelarPedido: CancelarPedidoUseCase,
    private val recuperarPedidoEnCarrito: RecuperarPedidoEnCarritoUseCase,
    private val calificarPedido: CalificarPedidoUseCase,
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

    fun volverALaCarta() {
        val pedido = _uiState.value.pedido ?: return
        viewModelScope.launch {
            if (pedido.estado == EstadoPedido.RECHAZADO) recuperarPedidoEnCarrito(pedido)
            _uiState.update { it.copy(irALaCarta = pedido.restauranteId) }
        }
    }

    fun navegacionHecha() {
        _uiState.update { it.copy(irALaCarta = null) }
    }

    fun elegirEstrellas(estrellas: Int) {
        actualizarCalificacion { it.copy(estrellas = estrellas) }
    }

    fun escribirComentario(comentario: String) {
        actualizarCalificacion { it.copy(comentario = comentario) }
    }

    fun votarPlato(platoId: String, meGusto: Boolean) {
        actualizarCalificacion {
            val votos = if (it.votos[platoId] == meGusto) it.votos - platoId else it.votos + (platoId to meGusto)
            it.copy(votos = votos)
        }
    }

    fun enviarCalificacion() {
        val pedido = _uiState.value.pedido ?: return
        val calificacion = _uiState.value.calificacion
        if (calificacion.estrellas == 0 || calificacion.enviando) return
        actualizarCalificacion { it.copy(enviando = true, error = null) }
        viewModelScope.launch {
            try {
                calificarPedido(pedido.id, calificacion.estrellas, calificacion.comentario, calificacion.votos)
                actualizarCalificacion { it.copy(enviando = false, enviada = true) }
            } catch (e: ErrorCalificar.YaCalificado) {
                actualizarCalificacion { it.copy(enviando = false, enviada = true, yaCalificada = true) }
            } catch (e: ErrorCalificar) {
                actualizarCalificacion { it.copy(enviando = false, error = e) }
            }
        }
    }

    fun errorCalificarMostrado() {
        actualizarCalificacion { it.copy(error = null) }
    }

    private fun actualizarCalificacion(cambio: (CalificacionUiState) -> CalificacionUiState) {
        _uiState.update { it.copy(calificacion = cambio(it.calificacion)) }
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
