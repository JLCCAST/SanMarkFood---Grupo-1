package com.equipo.sanmarkfood.restaurante.presentation.pedidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.ErrorPedidos
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.EstadoPedido
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.Modalidad
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.restaurante.domain.usecase.pedidos.AceptarPedidoUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.pedidos.AvanzarPedidoUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.pedidos.EntregarPedidoUseCase
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

sealed interface AvisoBandeja {
    data class Aceptado(val comensal: String, val listoA: Date) : AvisoBandeja
    data class Avanzado(val numero: Int, val modalidad: Modalidad) : AvisoBandeja
    data class Entregado(val numero: Int) : AvisoBandeja
    data class Fallo(val error: ErrorPedidos) : AvisoBandeja
}

data class PedidosUiState(
    val cargando: Boolean = true,
    val nuevos: List<Pedido> = emptyList(),
    val enPreparacion: List<Pedido> = emptyList(),
    val listos: List<Pedido> = emptyList(),
    val enCamino: List<Pedido> = emptyList(),
    val sinConexion: Boolean = false,
    val error: ErrorPedidos? = null,
    val hojaAceptar: HojaAceptar? = null,
    val procesando: Set<String> = emptySet(),
    val aviso: AvisoBandeja? = null,
)

@HiltViewModel
class PedidosViewModel @Inject constructor(
    private val observarPedidosDeHoy: ObservarPedidosDeHoyUseCase,
    private val aceptarPedido: AceptarPedidoUseCase,
    private val avanzarPedido: AvanzarPedidoUseCase,
    private val entregarPedido: EntregarPedidoUseCase,
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
                    it.copy(hojaAceptar = null, aviso = AvisoBandeja.Aceptado(hoja.pedido.comensalNombre, listoA))
                }
            } catch (e: ErrorPedidos) {
                _uiState.update { it.copy(hojaAceptar = it.hojaAceptar?.copy(enviando = false, error = e)) }
            }
        }
    }

    fun onAvanzar(pedido: Pedido) = procesar(pedido) {
        avanzarPedido(pedido)
        AvisoBandeja.Avanzado(pedido.numero, pedido.modalidad)
    }

    fun onEntregar(pedido: Pedido) = procesar(pedido) {
        entregarPedido(pedido)
        AvisoBandeja.Entregado(pedido.numero)
    }

    fun onAvisoMostrado() = _uiState.update { it.copy(aviso = null) }

    private fun procesar(pedido: Pedido, accion: suspend () -> AvisoBandeja) {
        if (pedido.id in _uiState.value.procesando) return

        _uiState.update { it.copy(procesando = it.procesando + pedido.id) }
        viewModelScope.launch {
            val aviso = try {
                accion()
            } catch (e: ErrorPedidos) {
                AvisoBandeja.Fallo(e)
            }
            _uiState.update { it.copy(procesando = it.procesando - pedido.id, aviso = aviso) }
        }
    }

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
