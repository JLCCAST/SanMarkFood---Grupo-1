package com.equipo.sanmarkfood.comensal.presentation.pedidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Carrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MetodoPago
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Modalidad
import com.equipo.sanmarkfood.comensal.domain.model.perfil.Direccion
import com.equipo.sanmarkfood.comensal.domain.repository.AuthRepository
import com.equipo.sanmarkfood.comensal.domain.usecase.pedidos.CambiarCantidadCarritoUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.pedidos.ObservarCarritoUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.pedidos.QuitarDelCarritoUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.perfil.AgregarDireccionUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.perfil.ObservarPerfilUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TuPedidoUiState(
    val cargando: Boolean = true,
    val carrito: Carrito? = null,
    val modalidad: Modalidad = Modalidad.RECOJO,
    val metodoPago: MetodoPago = MetodoPago.BILLETERA,
    val direcciones: List<Direccion> = emptyList(),
    val direccionElegida: Direccion? = null,
    val agregandoDireccion: Boolean = false,
    val guardandoDireccion: Boolean = false,
    val errorDireccion: Boolean = false,
)

@HiltViewModel
class TuPedidoViewModel @Inject constructor(
    observarCarrito: ObservarCarritoUseCase,
    private val observarPerfil: ObservarPerfilUseCase,
    private val cambiarCantidadCarrito: CambiarCantidadCarritoUseCase,
    private val quitarDelCarrito: QuitarDelCarritoUseCase,
    private val agregarDireccion: AgregarDireccionUseCase,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TuPedidoUiState())
    val uiState: StateFlow<TuPedidoUiState> = _uiState.asStateFlow()

    private val uid: String?
        get() = authRepository.currentUser?.uid

    init {
        viewModelScope.launch {
            observarCarrito().collect { carrito ->
                _uiState.update { it.copy(cargando = false, carrito = carrito) }
            }
        }
        escucharDirecciones()
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

    fun elegirDireccion(direccion: Direccion) {
        _uiState.update { it.copy(direccionElegida = direccion) }
    }

    fun abrirNuevaDireccion() {
        _uiState.update { it.copy(agregandoDireccion = true, errorDireccion = false) }
    }

    fun cerrarNuevaDireccion() {
        _uiState.update { it.copy(agregandoDireccion = false, errorDireccion = false) }
    }

    fun guardarDireccion(etiqueta: String, texto: String) {
        val usuario = uid ?: return
        val nueva = Direccion(etiqueta = etiqueta.trim(), direccionTexto = texto.trim())
        _uiState.update { it.copy(guardandoDireccion = true, errorDireccion = false) }
        viewModelScope.launch {
            agregarDireccion(usuario, _uiState.value.direcciones, nueva)
                .onSuccess {
                    _uiState.update {
                        it.copy(guardandoDireccion = false, agregandoDireccion = false, direccionElegida = nueva)
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(guardandoDireccion = false, errorDireccion = true) }
                }
        }
    }

    private fun escucharDirecciones() {
        val usuario = uid ?: return
        viewModelScope.launch {
            observarPerfil(usuario)
                .catch { }
                .collect { comensal ->
                    _uiState.update { estado ->
                        val direcciones = comensal.direcciones
                        estado.copy(
                            direcciones = direcciones,
                            direccionElegida = estado.direccionElegida?.takeIf { it in direcciones }
                                ?: direcciones.firstOrNull(),
                        )
                    }
                }
        }
    }
}
