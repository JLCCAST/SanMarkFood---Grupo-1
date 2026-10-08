package com.equipo.sanmarkfood.restaurante.presentation.menu

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.equipo.sanmarkfood.restaurante.core.navigation.ArmarMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.BorradorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.CampoMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.DatosMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.MenuDelDia
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OrigenMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.fechaDeHoy
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.EditarMenuDelDiaUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.GuardarBorradorMenuUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.ObtenerBorradorMenuUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.ObtenerMenuDeAyerUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.ObtenerMenuDeHoyUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.PublicarMenuDelDiaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArmarMenuUiState(
    val fecha: String = fechaDeHoy(),
    val cargando: Boolean = false,
    val errorCarga: ErrorMenu? = null,
    val origen: OrigenMenu? = null,
    val editando: Boolean = false,
    val precio: String = "",
    val entradas: List<String> = emptyList(),
    val nuevaEntrada: String = "",
    val segundos: List<String> = emptyList(),
    val nuevoSegundo: String = "",
    val refresco: String = "",
    val postre: String = "",
    val horaFin: String = DatosMenu.HORA_FIN_POR_DEFECTO,
    val camposInvalidos: Set<CampoMenu> = emptySet(),
    val publicando: Boolean = false,
    val error: ErrorMenu? = null,
    val publicado: Boolean = false,
) {
    val completo: Boolean get() = precio.isNotBlank() && entradas.isNotEmpty() && segundos.isNotEmpty()
}

@HiltViewModel
class ArmarMenuViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val obtenerMenuDeAyer: ObtenerMenuDeAyerUseCase,
    private val obtenerMenuDeHoy: ObtenerMenuDeHoyUseCase,
    private val obtenerBorradorMenu: ObtenerBorradorMenuUseCase,
    private val guardarBorradorMenu: GuardarBorradorMenuUseCase,
    private val publicarMenuDelDia: PublicarMenuDelDiaUseCase,
    private val editarMenuDelDia: EditarMenuDelDiaUseCase,
) : ViewModel() {

    private val modo: ModoArmarMenu = savedStateHandle.toRoute<ArmarMenu>().modo

    private val origenDelBorrador: OrigenMenu? = when (modo) {
        ModoArmarMenu.CERO -> OrigenMenu.CERO
        ModoArmarMenu.COPIAR_AYER -> OrigenMenu.AYER
        ModoArmarMenu.IA -> OrigenMenu.IA
        ModoArmarMenu.EDITAR -> null
    }

    private val _uiState = MutableStateFlow(ArmarMenuUiState(cargando = true, editando = modo == ModoArmarMenu.EDITAR))
    val uiState: StateFlow<ArmarMenuUiState> = _uiState.asStateFlow()

    private var menuPublicado: MenuDelDia? = null

    private var guardado: Job? = null

    init {
        viewModelScope.launch {
            val borrador = obtenerBorradorMenu()
            if (borrador != null && borrador.origen == origenDelBorrador) {
                mostrarBorrador(borrador)
            } else {
                cargarMenu()
            }
        }
    }

    fun onReintentarCarga() = cargarMenu()

    fun onCambiarPrecio(precio: String) =
        cambiarCampo(CampoMenu.PRECIO) { it.copy(precio = precio.take(MAX_PRECIO)) }

    fun onCambiarNuevaEntrada(texto: String) =
        _uiState.update { it.copy(nuevaEntrada = texto.take(DatosMenu.MAX_TEXTO)) }

    fun onAgregarEntrada() {
        val nueva = _uiState.value.nuevaEntrada.trim()
        if (nueva.isNotEmpty()) {
            cambiarCampo(CampoMenu.ENTRADAS) { it.copy(entradas = it.entradas + nueva, nuevaEntrada = "") }
        }
    }

    fun onQuitarEntrada(indice: Int) =
        cambiarBorrador { it.copy(entradas = it.entradas.filterIndexed { i, _ -> i != indice }) }

    fun onCambiarNuevoSegundo(texto: String) =
        _uiState.update { it.copy(nuevoSegundo = texto.take(DatosMenu.MAX_TEXTO)) }

    fun onAgregarSegundo() {
        val nuevo = _uiState.value.nuevoSegundo.trim()
        if (nuevo.isNotEmpty()) {
            cambiarCampo(CampoMenu.SEGUNDOS) { it.copy(segundos = it.segundos + nuevo, nuevoSegundo = "") }
        }
    }

    fun onQuitarSegundo(indice: Int) =
        cambiarBorrador { it.copy(segundos = it.segundos.filterIndexed { i, _ -> i != indice }) }

    fun onCambiarRefresco(refresco: String) = cambiarBorrador { it.copy(refresco = refresco.take(DatosMenu.MAX_TEXTO)) }

    fun onCambiarPostre(postre: String) = cambiarBorrador { it.copy(postre = postre.take(DatosMenu.MAX_TEXTO)) }

    fun onElegirHoraFin(hora: String) = cambiarBorrador { it.copy(horaFin = hora) }

    fun onPublicar() {
        val estado = _uiState.value
        val menuPublicado = menuPublicado
        if (estado.publicando || (estado.editando && menuPublicado == null)) return

        _uiState.update { it.copy(publicando = true, error = null) }
        viewModelScope.launch {
            try {
                if (menuPublicado != null) {
                    editarMenuDelDia(
                        menu = menuPublicado,
                        precio = estado.precio,
                        entradas = estado.entradas,
                        segundos = estado.segundos,
                        refresco = estado.refresco,
                        postre = estado.postre,
                        horaFin = estado.horaFin,
                    )
                } else {
                    publicarMenuDelDia(
                        precio = estado.precio,
                        entradas = estado.entradas,
                        segundos = estado.segundos,
                        refresco = estado.refresco,
                        postre = estado.postre,
                        horaFin = estado.horaFin,
                        origen = estado.origen ?: OrigenMenu.CERO,
                    )
                }
                _uiState.update { it.copy(publicando = false, publicado = true) }
            } catch (e: ErrorMenu.DatosMenuInvalidos) {
                _uiState.update { it.copy(publicando = false, camposInvalidos = e.campos, error = e) }
            } catch (e: ErrorMenu) {
                _uiState.update { it.copy(publicando = false, error = e) }
            }
        }
    }

    private fun mostrarBorrador(borrador: BorradorMenu) = _uiState.update {
        it.copy(
            cargando = false,
            origen = borrador.origen,
            precio = borrador.precio,
            entradas = borrador.entradas,
            segundos = borrador.segundos,
            refresco = borrador.refresco,
            postre = borrador.postre,
            horaFin = borrador.horaFin,
        )
    }

    private fun cargarMenu() {
        if (modo == ModoArmarMenu.CERO || modo == ModoArmarMenu.IA) {
            _uiState.update { it.copy(cargando = false) }
            return
        }

        _uiState.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            try {
                val menu = if (modo == ModoArmarMenu.EDITAR) obtenerMenuDeHoy() else obtenerMenuDeAyer()
                if (modo == ModoArmarMenu.EDITAR) menuPublicado = menu ?: throw ErrorMenu.Desconocido
                val datos = menu?.datos
                _uiState.update { estado ->
                    if (datos == null) {
                        estado.copy(cargando = false)
                    } else {
                        estado.copy(
                            cargando = false,
                            origen = if (modo == ModoArmarMenu.COPIAR_AYER) OrigenMenu.AYER else null,
                            precio = precioEnSoles(datos.precio),
                            entradas = datos.entradas.map { it.nombre },
                            segundos = datos.segundos.map { it.nombre },
                            refresco = datos.refresco.orEmpty(),
                            postre = datos.postre.orEmpty(),
                            horaFin = datos.horaFin,
                        )
                    }
                }
            } catch (e: ErrorMenu) {
                _uiState.update { it.copy(cargando = false, errorCarga = e) }
            }
        }
    }

    private fun cambiarCampo(campo: CampoMenu, cambio: (ArmarMenuUiState) -> ArmarMenuUiState) {
        cambiarBorrador { estado ->
            val invalidos = estado.camposInvalidos - campo
            val error = estado.error.takeUnless { it is ErrorMenu.DatosMenuInvalidos && invalidos.isEmpty() }
            cambio(estado).copy(camposInvalidos = invalidos, error = error)
        }
    }

    private fun cambiarBorrador(cambio: (ArmarMenuUiState) -> ArmarMenuUiState) {
        _uiState.update(cambio)
        val origen = origenDelBorrador ?: return
        val estado = _uiState.value
        guardado?.cancel()
        guardado = viewModelScope.launch {
            guardarBorradorMenu(
                BorradorMenu(
                    origen = origen,
                    precio = estado.precio,
                    entradas = estado.entradas,
                    segundos = estado.segundos,
                    refresco = estado.refresco,
                    postre = estado.postre,
                    horaFin = estado.horaFin,
                )
            )
        }
    }

    private companion object {
        const val MAX_PRECIO = 8
    }
}
