package com.equipo.sanmarkfood.comensal.presentation.pedidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.comensal.domain.usecase.pedidos.ObservarPedidoEnCursoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class PedidoEnCursoUiState(
    val pedido: Pedido? = null,
)

@HiltViewModel
class PedidoEnCursoViewModel @Inject constructor(
    observarPedidoEnCurso: ObservarPedidoEnCursoUseCase,
) : ViewModel() {

    val uiState: StateFlow<PedidoEnCursoUiState> = observarPedidoEnCurso()
        .map { PedidoEnCursoUiState(pedido = it) }
        .catch { emit(PedidoEnCursoUiState()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(ESPERA_AL_SALIR), PedidoEnCursoUiState())

    private companion object {
        const val ESPERA_AL_SALIR = 5_000L
    }
}
