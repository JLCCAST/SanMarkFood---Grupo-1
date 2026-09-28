package com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.CampoLocal
import com.equipo.sanmarkfood.restaurante.domain.model.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.Ubicacion
import com.equipo.sanmarkfood.restaurante.domain.usecase.CerrarSesionUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.GuardarDatosLocalUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.ObtenerRestauranteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DatosLocalUiState(
    val cargando: Boolean = true,
    val errorCarga: ErrorRestaurante? = null,
    val nombre: String = "",
    val categoria: CategoriaRestaurante? = null,
    val direccion: String = "",
    val ubicacion: Ubicacion = Ubicacion.CiudadUniversitaria,
    val telefono: String = "",
    val eligiendoUbicacion: Boolean = false,
    val camposInvalidos: Set<CampoLocal> = emptySet(),
    val guardando: Boolean = false,
    val error: ErrorRestaurante? = null,
    val guardado: Boolean = false,
    val sesionCerrada: Boolean = false,
)

@HiltViewModel
class DatosLocalViewModel @Inject constructor(
    private val obtenerRestaurante: ObtenerRestauranteUseCase,
    private val guardarDatosLocal: GuardarDatosLocalUseCase,
    private val cerrarSesion: CerrarSesionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DatosLocalUiState())
    val uiState: StateFlow<DatosLocalUiState> = _uiState.asStateFlow()

    init {
        cargarLocal()
    }

    fun onReintentarCarga() = cargarLocal()

    fun onCambiarNombre(nombre: String) =
        cambiarCampo(CampoLocal.NOMBRE) { it.copy(nombre = nombre.take(DatosLocal.MAX_NOMBRE)) }

    fun onElegirCategoria(categoria: CategoriaRestaurante) =
        cambiarCampo(CampoLocal.CATEGORIA) { it.copy(categoria = categoria) }

    fun onCambiarDireccion(direccion: String) =
        cambiarCampo(CampoLocal.DIRECCION) { it.copy(direccion = direccion.take(DatosLocal.MAX_DIRECCION)) }

    fun onCambiarTelefono(telefono: String) =
        cambiarCampo(CampoLocal.TELEFONO) { it.copy(telefono = telefono.take(MAX_TELEFONO)) }

    fun onMoverPunto() = _uiState.update { it.copy(eligiendoUbicacion = true) }

    fun onCancelarUbicacion() = _uiState.update { it.copy(eligiendoUbicacion = false) }

    fun onUbicacionElegida(ubicacion: Ubicacion) =
        _uiState.update { it.copy(ubicacion = ubicacion, eligiendoUbicacion = false) }

    fun onContinuar() {
        val estado = _uiState.value
        if (estado.guardando) return

        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            try {
                guardarDatosLocal(estado.nombre, estado.categoria, estado.direccion, estado.ubicacion, estado.telefono)
                _uiState.update { it.copy(guardando = false, guardado = true) }
            } catch (e: ErrorRestaurante.DatosInvalidos) {
                _uiState.update { it.copy(guardando = false, camposInvalidos = e.campos, error = e) }
            } catch (e: ErrorRestaurante) {
                _uiState.update { it.copy(guardando = false, error = e) }
            }
        }
    }

    // Se marca como atendido para que, al volver desde el paso siguiente, no se navegue otra vez.
    fun onGuardadoAtendido() = _uiState.update { it.copy(guardado = false) }

    // La cuenta ya está creada y verificada: volver al paso 1 no tiene sentido, así que
    // «atrás» cierra la sesión. Al volver a entrar, el arranque trae de nuevo a este paso.
    fun onVolver() {
        cerrarSesion()
        _uiState.update { it.copy(sesionCerrada = true) }
    }

    private fun cargarLocal() {
        _uiState.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            try {
                val datos = obtenerRestaurante()?.datos
                _uiState.update { estado ->
                    if (datos == null) {
                        estado.copy(cargando = false)
                    } else {
                        estado.copy(
                            cargando = false,
                            nombre = datos.nombre,
                            categoria = datos.categoria,
                            direccion = datos.direccion,
                            ubicacion = datos.ubicacion,
                            telefono = datos.telefono,
                        )
                    }
                }
            } catch (e: ErrorRestaurante) {
                _uiState.update { it.copy(cargando = false, errorCarga = e) }
            }
        }
    }

    // Al corregir un campo marcado se le quita la marca; con el último, también el aviso general.
    private fun cambiarCampo(campo: CampoLocal, cambio: (DatosLocalUiState) -> DatosLocalUiState) {
        _uiState.update { estado ->
            val invalidos = estado.camposInvalidos - campo
            val error = estado.error.takeUnless { it is ErrorRestaurante.DatosInvalidos && invalidos.isEmpty() }
            cambio(estado).copy(camposInvalidos = invalidos, error = error)
        }
    }

    private companion object {
        // «+51 987 654 321» con espacios es lo más largo que tiene sentido escribir.
        const val MAX_TELEFONO = 16
    }
}
