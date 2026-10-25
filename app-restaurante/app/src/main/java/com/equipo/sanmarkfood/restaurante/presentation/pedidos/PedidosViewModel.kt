package com.equipo.sanmarkfood.restaurante.presentation.pedidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.ErrorPedidos
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.EstadoPedido
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.restaurante.domain.usecase.pedidos.AceptarPedidoUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.pedidos.ObservarPedidosDeHoyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class HojaAceptar(
    val pedido: Pedido,
    val minutos: Int = 15,
    val enviando: Boolean = false,
    val error: ErrorPedidos? = null,
)

data class PedidoAceptado(val comensal: String, val listoA: Date)

data class PedidosUiState(
    val cargando: Boolean = true,
    val nuevos: List<Pedido> = emptyList(),
    val enPreparacion: List<Pedido> = emptyList(),
    val listos: List<Pedido> = emptyList(),
    val enCamino: List<Pedido> = emptyList(),
    val sinConexion: Boolean = false,
    val error: ErrorPedidos? = null,
    val hojaAceptar: HojaAceptar? = null,
    val aceptado: PedidoAceptado? = null,
)

@HiltViewModel
class PedidosViewModel @Inject constructor(
    private val observarPedidosDeHoy: ObservarPedidosDeHoyUseCase,
    private val aceptarPedido: AceptarPedidoUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PedidosUiState())
    val uiState: StateFlow<PedidosUiState> = _uiState.asStateFlow()

    private var observacion: Job? = null

    init {
        observar()
    }

    fun onReintentar() = observar()

    fun onAceptar(pedido: Pedido) = _uiState.update { it.copy(hojaAceptar = HojaAceptar(pedido)) }

    fun onElegirMinutos(minutos: Int) =
        _uiState.update { it.copy(hojaAceptar = it.hojaAceptar?.copy(minutos = minutos, error = null)) }

    fun onCerrarHoja() = _uiState.update { it.copy(hojaAceptar = null) }

    fun onConfirmarAceptar() {
        val hoja = _uiState.value.hojaAceptar ?: return
        if (hoja.enviando) return

        _uiState.update { it.copy(hojaAceptar = hoja.copy(enviando = true, error = null)) }
        viewModelScope.launch {
            try {
                aceptarPedido(hoja.pedido, hoja.minutos)
                val listoA = Date(System.currentTimeMillis() + hoja.minutos * 60_000L)
                _uiState.update {
                    it.copy(hojaAceptar = null, aceptado = PedidoAceptado(hoja.pedido.comensalNombre, listoA))
                }
            } catch (e: ErrorPedidos) {
                _uiState.update { it.copy(hojaAceptar = it.hojaAceptar?.copy(enviando = false, error = e)) }
            }
        }
    }

    fun onAceptadoMostrado() = _uiState.update { it.copy(aceptado = null) }

    private fun observar() {
        observacion?.cancel()
        _uiState.update { it.copy(cargando = true, error = null) }
        observacion = viewModelScope.launch {
            observarPedidosDeHoy()
                .catch { e -> if (e is ErrorPedidos) _uiState.update { it.copy(cargando = false, error = e) } else throw e }
                .collect { deHoy ->
                    val pedidos = deHoy.pedidos
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            nuevos = pedidos.filter { pedido -> pedido.estado == EstadoPedido.RECIBIDO },
                            enPreparacion = pedidos.filter { pedido -> pedido.estado == EstadoPedido.PREPARANDO },
                            listos = pedidos.filter { pedido -> pedido.estado == EstadoPedido.LISTO },
                            enCamino = pedidos.filter { pedido -> pedido.estado == EstadoPedido.EN_CAMINO },
                            sinConexion = deHoy.sinConexion,
                            error = null,
                        )
                    }
                }
        }
    }
}
