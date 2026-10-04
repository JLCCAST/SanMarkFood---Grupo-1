package com.equipo.sanmarkfood.restaurante.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.menu.CampoPlato
import com.equipo.sanmarkfood.restaurante.domain.model.menu.Categoria
import com.equipo.sanmarkfood.restaurante.domain.model.menu.DatosPlato
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.CrearCategoriaUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.CrearPlatoUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.ObservarCategoriasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlatoUiState(
    val fotoLocal: String? = null,
    val nombre: String = "",
    val descripcion: String = "",
    val precio: String = "",
    val categoriaId: String? = null,
    val categorias: List<Categoria>? = null,
    val errorCategorias: ErrorMenu? = null,
    val nuevaCategoria: NuevaCategoriaUiState? = null,
    val camposInvalidos: Set<CampoPlato> = emptySet(),
    val guardando: Boolean = false,
    val error: ErrorMenu? = null,
    val guardado: Boolean = false,
)

data class NuevaCategoriaUiState(
    val nombre: String = "",
    val creando: Boolean = false,
    val error: ErrorMenu? = null,
)

@HiltViewModel
class PlatoViewModel @Inject constructor(
    private val observarCategorias: ObservarCategoriasUseCase,
    private val crearCategoria: CrearCategoriaUseCase,
    private val crearPlato: CrearPlatoUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlatoUiState())
    val uiState: StateFlow<PlatoUiState> = _uiState.asStateFlow()

    private var observacion: Job? = null

    init {
        observar()
    }

    fun onReintentarCategorias() = observar()

    fun onFotoElegida(imagenLocal: String) = _uiState.update { it.copy(fotoLocal = imagenLocal, error = null) }

    fun onCambiarNombre(nombre: String) =
        cambiarCampo(CampoPlato.NOMBRE) { it.copy(nombre = nombre.take(DatosPlato.MAX_NOMBRE)) }

    fun onCambiarDescripcion(descripcion: String) =
        cambiarCampo(CampoPlato.DESCRIPCION) { it.copy(descripcion = descripcion.take(DatosPlato.MAX_DESCRIPCION)) }

    fun onCambiarPrecio(precio: String) =
        cambiarCampo(CampoPlato.PRECIO) { it.copy(precio = precio.take(MAX_PRECIO)) }

    fun onElegirCategoria(categoriaId: String) =
        cambiarCampo(CampoPlato.CATEGORIA) { it.copy(categoriaId = categoriaId) }

    fun onGuardar() {
        val estado = _uiState.value
        if (estado.guardando) return

        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            try {
                crearPlato(estado.nombre, estado.descripcion, estado.precio, estado.categoriaId, estado.fotoLocal)
                _uiState.update { it.copy(guardando = false, guardado = true) }
            } catch (e: ErrorMenu.DatosPlatoInvalidos) {
                _uiState.update { it.copy(guardando = false, camposInvalidos = e.campos, error = e) }
            } catch (e: ErrorMenu) {
                _uiState.update { it.copy(guardando = false, error = e) }
            }
        }
    }

    fun onNuevaCategoria() = _uiState.update { it.copy(nuevaCategoria = NuevaCategoriaUiState()) }

    fun onCambiarNombreCategoria(nombre: String) = _uiState.update { estado ->
        estado.copy(nuevaCategoria = estado.nuevaCategoria?.copy(nombre = nombre.take(Categoria.MAX_NOMBRE), error = null))
    }

    fun onCerrarNuevaCategoria() = _uiState.update {
        if (it.nuevaCategoria?.creando == true) it else it.copy(nuevaCategoria = null)
    }

    fun onCrearCategoria() {
        val dialogo = _uiState.value.nuevaCategoria ?: return
        if (dialogo.creando) return

        _uiState.update { it.copy(nuevaCategoria = dialogo.copy(creando = true, error = null)) }
        viewModelScope.launch {
            try {
                val categoria = crearCategoria(dialogo.nombre)
                _uiState.update { it.copy(nuevaCategoria = null) }
                onElegirCategoria(categoria.id)
            } catch (e: ErrorMenu) {
                _uiState.update { it.copy(nuevaCategoria = it.nuevaCategoria?.copy(creando = false, error = e)) }
            }
        }
    }

    private fun observar() {
        observacion?.cancel()
        _uiState.update { it.copy(errorCategorias = null) }
        observacion = viewModelScope.launch {
            observarCategorias()
                .catch { e -> if (e is ErrorMenu) _uiState.update { it.copy(errorCategorias = e) } else throw e }
                .collect { categorias -> _uiState.update { it.copy(categorias = categorias, errorCategorias = null) } }
        }
    }

    private fun cambiarCampo(campo: CampoPlato, cambio: (PlatoUiState) -> PlatoUiState) {
        _uiState.update { estado ->
            val invalidos = estado.camposInvalidos - campo
            val error = estado.error.takeUnless { it is ErrorMenu.DatosPlatoInvalidos && invalidos.isEmpty() }
            cambio(estado).copy(camposInvalidos = invalidos, error = error)
        }
    }

    private companion object {
        const val MAX_PRECIO = 8
    }
}
