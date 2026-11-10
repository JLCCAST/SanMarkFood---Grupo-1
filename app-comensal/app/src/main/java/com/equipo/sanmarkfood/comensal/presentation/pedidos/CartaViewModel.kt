package com.equipo.sanmarkfood.comensal.presentation.pedidos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.equipo.sanmarkfood.comensal.core.navigation.DetalleRestaurante
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Carrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.DisponibilidadMenu
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ErrorPedidos
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ItemCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MenuDeHoy
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MenuDelCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Plato
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.PlatoDelCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ResultadoAgregarAlCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.SeccionCarta
import com.equipo.sanmarkfood.comensal.domain.usecase.pedidos.AgregarAlCarritoUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.pedidos.CambiarCantidadCarritoUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.pedidos.ObservarCarritoUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.pedidos.ObservarCartaUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.pedidos.ObservarDisponibilidadMenuUseCase
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

data class ItemPendiente(
    val item: ItemCarrito,
    val restauranteNombre: String,
    val restauranteActual: String,
)

data class ArmadoMenu(
    val entrada: String?,
    val segundo: String?,
)

data class CartaUiState(
    val cargando: Boolean = true,
    val secciones: List<SeccionCarta> = emptyList(),
    val disponibilidadMenu: DisponibilidadMenu? = null,
    val armado: ArmadoMenu? = null,
    val carrito: Carrito? = null,
    val error: ErrorPedidos? = null,
    val itemPendiente: ItemPendiente? = null,
    val limiteAlcanzado: Boolean = false,
) {
    val menu: MenuDeHoy?
        get() = (disponibilidadMenu as? DisponibilidadMenu.Disponible)?.menu

    val menuArmado: MenuDelCarrito?
        get() {
            val entrada = armado?.entrada ?: return null
            val segundo = armado.segundo ?: return null
            return menu?.armar(entrada, segundo)
        }
}

@HiltViewModel
class CartaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observarCarta: ObservarCartaUseCase,
    private val observarDisponibilidadMenu: ObservarDisponibilidadMenuUseCase,
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
        val item = PlatoDelCarrito(
            platoId = plato.id,
            nombre = plato.nombre,
            precio = plato.precio,
            cantidad = 1,
            fotoUrl = plato.fotoUrl,
        )
        viewModelScope.launch { resolver(item, restauranteNombre, vaciarSiOtroLocal = false) }
    }

    fun abrirArmadoMenu() {
        val menu = _uiState.value.menu ?: return
        _uiState.update {
            it.copy(
                armado = ArmadoMenu(
                    entrada = menu.entradas.firstOrNull { opcion -> !opcion.agotado }?.nombre,
                    segundo = menu.segundos.firstOrNull { opcion -> !opcion.agotado }?.nombre,
                )
            )
        }
    }

    fun elegirEntrada(nombre: String) {
        _uiState.update { it.copy(armado = it.armado?.copy(entrada = nombre)) }
    }

    fun elegirSegundo(nombre: String) {
        _uiState.update { it.copy(armado = it.armado?.copy(segundo = nombre)) }
    }

    fun cerrarArmadoMenu() {
        _uiState.update { it.copy(armado = null) }
    }

    fun agregarMenu(restauranteNombre: String) {
        val item = _uiState.value.menuArmado ?: return
        _uiState.update { it.copy(armado = null) }
        viewModelScope.launch { resolver(item, restauranteNombre, vaciarSiOtroLocal = false) }
    }

    fun vaciarYAgregar() {
        val pendiente = _uiState.value.itemPendiente ?: return
        _uiState.update { it.copy(itemPendiente = null) }
        viewModelScope.launch { resolver(pendiente.item, pendiente.restauranteNombre, vaciarSiOtroLocal = true) }
    }

    fun mantenerActual() {
        _uiState.update { it.copy(itemPendiente = null) }
    }

    fun limpiarLimite() {
        _uiState.update { it.copy(limiteAlcanzado = false) }
    }

    fun cambiarCantidad(clave: String, cantidad: Int) {
        viewModelScope.launch { cambiarCantidadCarrito(clave, cantidad) }
    }

    fun quitar(clave: String) {
        viewModelScope.launch { quitarDelCarrito(clave) }
    }

    private fun escucharCarta() {
        cartaJob?.cancel()
        cartaJob = viewModelScope.launch {
            launch {
                observarDisponibilidadMenu(restauranteId)
                    .catch { _uiState.update { it.copy(disponibilidadMenu = null, armado = null) } }
                    .collect { disponibilidad ->
                        _uiState.update {
                            it.copy(
                                disponibilidadMenu = disponibilidad,
                                armado = it.armado.takeIf { disponibilidad is DisponibilidadMenu.Disponible },
                            )
                        }
                    }
            }
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

    private suspend fun resolver(item: ItemCarrito, restauranteNombre: String, vaciarSiOtroLocal: Boolean) {
        when (val resultado = agregarAlCarrito(restauranteId, restauranteNombre, item, vaciarSiOtroLocal)) {
            ResultadoAgregarAlCarrito.Agregado -> Unit
            is ResultadoAgregarAlCarrito.OtroLocal -> _uiState.update {
                it.copy(itemPendiente = ItemPendiente(item, restauranteNombre, resultado.restauranteActual))
            }
            ResultadoAgregarAlCarrito.LimiteAlcanzado -> _uiState.update { it.copy(limiteAlcanzado = true) }
        }
    }
}