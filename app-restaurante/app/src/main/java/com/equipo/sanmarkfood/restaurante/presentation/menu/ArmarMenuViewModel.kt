package com.equipo.sanmarkfood.restaurante.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.menu.CampoMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.DatosMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OrigenMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.fechaDeHoy
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.PublicarMenuDelDiaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArmarMenuUiState(
    val fecha: String = fechaDeHoy(),
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
    private val publicarMenuDelDia: PublicarMenuDelDiaUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArmarMenuUiState())
    val uiState: StateFlow<ArmarMenuUiState> = _uiState.asStateFlow()

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
        _uiState.update { it.copy(entradas = it.entradas.filterIndexed { i, _ -> i != indice }) }

    fun onCambiarNuevoSegundo(texto: String) =
        _uiState.update { it.copy(nuevoSegundo = texto.take(DatosMenu.MAX_TEXTO)) }

    fun onAgregarSegundo() {
        val nuevo = _uiState.value.nuevoSegundo.trim()
        if (nuevo.isNotEmpty()) {
            cambiarCampo(CampoMenu.SEGUNDOS) { it.copy(segundos = it.segundos + nuevo, nuevoSegundo = "") }
        }
    }

    fun onQuitarSegundo(indice: Int) =
        _uiState.update { it.copy(segundos = it.segundos.filterIndexed { i, _ -> i != indice }) }

    fun onCambiarRefresco(refresco: String) = _uiState.update { it.copy(refresco = refresco.take(DatosMenu.MAX_TEXTO)) }

    fun onCambiarPostre(postre: String) = _uiState.update { it.copy(postre = postre.take(DatosMenu.MAX_TEXTO)) }

    fun onElegirHoraFin(hora: String) = _uiState.update { it.copy(horaFin = hora) }

    fun onPublicar() {
        val estado = _uiState.value
        if (estado.publicando) return

        _uiState.update { it.copy(publicando = true, error = null) }
        viewModelScope.launch {
            try {
                publicarMenuDelDia(
                    precio = estado.precio,
                    entradas = estado.entradas,
                    segundos = estado.segundos,
                    refresco = estado.refresco,
                    postre = estado.postre,
                    horaFin = estado.horaFin,
                    origen = OrigenMenu.CERO,
                )
                _uiState.update { it.copy(publicando = false, publicado = true) }
            } catch (e: ErrorMenu.DatosMenuInvalidos) {
                _uiState.update { it.copy(publicando = false, camposInvalidos = e.campos, error = e) }
            } catch (e: ErrorMenu) {
                _uiState.update { it.copy(publicando = false, error = e) }
            }
        }
    }

    private fun cambiarCampo(campo: CampoMenu, cambio: (ArmarMenuUiState) -> ArmarMenuUiState) {
        _uiState.update { estado ->
            val invalidos = estado.camposInvalidos - campo
            val error = estado.error.takeUnless { it is ErrorMenu.DatosMenuInvalidos && invalidos.isEmpty() }
            cambio(estado).copy(camposInvalidos = invalidos, error = error)
        }
    }

    private companion object {
        const val MAX_PRECIO = 8
    }
}
