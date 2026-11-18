package com.equipo.sanmarkfood.restaurante.presentation.pedidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.ErrorPedidos
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.listoConCodigo
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

const val DIGITOS_CODIGO = 4

data class EntregarCodigoUiState(
    val codigo: String = "",
    val pedidos: List<Pedido> = emptyList(),
    val cargando: Boolean = true,
    val entregando: Boolean = false,
    val entregado: Pedido? = null,
    val error: ErrorPedidos? = null,
) {
    val completo: Boolean get() = codigo.length == DIGITOS_CODIGO

    val encontrado: Pedido? get() = if (completo && entregado == null) pedidos.listoConCodigo(codigo) else null

    val noEncontrado: Boolean get() = completo && entregado == null && !cargando && encontrado == null
}

@HiltViewModel
class EntregarCodigoViewModel @Inject constructor(
    observarPedidosDeHoy: ObservarPedidosDeHoyUseCase,
    private val entregarPedido: EntregarPedidoUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EntregarCodigoUiState())
    val uiState: StateFlow<EntregarCodigoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observarPedidosDeHoy()
                .catch { e -> if (e is ErrorPedidos) _uiState.update { it.copy(cargando = false, error = e) } else throw e }
                .collect { deHoy -> _uiState.update { it.copy(cargando = false, pedidos = deHoy.pedidos) } }
        }
    }

    fun onDigito(digito: Char) = _uiState.update {
        if (it.completo || it.entregado != null) it else it.copy(codigo = it.codigo + digito, error = null)
    }

    fun onBorrar() = _uiState.update {
        if (it.entregando || it.entregado != null) it else it.copy(codigo = it.codigo.dropLast(1), error = null)
    }

    fun onEntregar() {
        val estado = _uiState.value
        val pedido = estado.encontrado ?: return
        if (estado.entregando) return

        _uiState.update { it.copy(entregando = true, error = null) }
        viewModelScope.launch {
            try {
                entregarPedido(pedido, estado.codigo)
                _uiState.update { it.copy(entregando = false, entregado = pedido) }
            } catch (e: ErrorPedidos) {
                _uiState.update { it.copy(entregando = false, error = e) }
            }
        }
    }

    fun onEntregarOtro() = _uiState.update { it.copy(codigo = "", entregado = null, error = null) }
}
