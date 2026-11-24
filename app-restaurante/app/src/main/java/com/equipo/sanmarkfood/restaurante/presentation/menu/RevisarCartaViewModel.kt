package com.equipo.sanmarkfood.restaurante.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.PlatoLeido
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.AgregarPlatosLeidosUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.ObtenerCartaLeidaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FilaLeida(
    val id: Int,
    val categoria: String,
    val nombre: String,
    val precio: String,
    val nombreDudoso: Boolean,
    val precioDudoso: Boolean,
    val marcada: Boolean = true,
    val invalida: Boolean = false,
)

data class RevisarCartaUiState(
    val filas: List<FilaLeida> = emptyList(),
    val agregando: Boolean = false,
    val agregada: Boolean = false,
    val error: ErrorMenu? = null,
) {
    val secciones: Map<String, List<FilaLeida>> get() = filas.groupBy { it.categoria }
    val marcadas: Int get() = filas.count { it.marcada }
}

@HiltViewModel
class RevisarCartaViewModel @Inject constructor(
    obtenerCartaLeida: ObtenerCartaLeidaUseCase,
    private val agregarPlatosLeidos: AgregarPlatosLeidosUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RevisarCartaUiState(
            filas = obtenerCartaLeida().mapIndexed { indice, plato ->
                FilaLeida(
                    id = indice,
                    categoria = plato.categoria,
                    nombre = plato.nombre,
                    precio = plato.precio,
                    nombreDudoso = plato.nombreDudoso,
                    precioDudoso = plato.precioDudoso,
                )
            },
        )
    )
    val uiState: StateFlow<RevisarCartaUiState> = _uiState.asStateFlow()

    fun onCambiarNombre(id: Int, nombre: String) =
        actualizar(id) { it.copy(nombre = nombre, nombreDudoso = false, invalida = false) }

    fun onCambiarPrecio(id: Int, precio: String) =
        actualizar(id) { it.copy(precio = precio, precioDudoso = false, invalida = false) }

    fun onMarcar(id: Int, marcada: Boolean) = actualizar(id) { it.copy(marcada = marcada) }

    fun onAgregar() {
        val estado = _uiState.value
        val marcadas = estado.filas.filter { it.marcada }
        if (marcadas.isEmpty() || estado.agregando) return
        _uiState.update { it.copy(agregando = true, error = null) }
        viewModelScope.launch {
            try {
                agregarPlatosLeidos(marcadas.map { PlatoLeido(it.nombre, it.precio, it.categoria) })
                _uiState.update { it.copy(agregando = false, agregada = true) }
            } catch (e: ErrorMenu.PlatosLeidosInvalidos) {
                val invalidas = e.filas.map { marcadas[it].id }.toSet()
                _uiState.update { actual ->
                    actual.copy(
                        agregando = false,
                        error = e,
                        filas = actual.filas.map { if (it.id in invalidas) it.copy(invalida = true) else it },
                    )
                }
            } catch (e: ErrorMenu) {
                _uiState.update { it.copy(agregando = false, error = e) }
            }
        }
    }

    private fun actualizar(id: Int, cambio: (FilaLeida) -> FilaLeida) = _uiState.update { estado ->
        if (estado.agregando) estado else estado.copy(filas = estado.filas.map { if (it.id == id) cambio(it) else it })
    }
}
