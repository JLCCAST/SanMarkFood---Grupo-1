package com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
// La ruta se llama igual que el modelo de dominio DatosLocal, que también se usa aquí.
import com.equipo.sanmarkfood.restaurante.core.navigation.DatosLocal as RutaDatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.CampoLocal
import com.equipo.sanmarkfood.restaurante.domain.model.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.MotivoRechazo
import com.equipo.sanmarkfood.restaurante.domain.model.Rechazo
import com.equipo.sanmarkfood.restaurante.domain.model.TipoFoto
import com.equipo.sanmarkfood.restaurante.domain.model.Ubicacion
import com.equipo.sanmarkfood.restaurante.domain.usecase.CerrarSesionUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.GuardarDatosLocalUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.ObtenerRestauranteUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.ReenviarARevisionUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.SubirFotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DatosLocalUiState(
    val modo: ModoFormulario = ModoFormulario.ALTA,
    val cargando: Boolean = true,
    val errorCarga: ErrorRestaurante? = null,
    /** Solo en R7: el motivo del rechazo que se muestra arriba del formulario. */
    val rechazo: Rechazo? = null,
    /** En R7, si rechazaron la dirección: se marca en rojo hasta que la cambien o muevan el punto. */
    val direccionPorCorregir: Boolean = false,
    val portada: FotoUiState = FotoUiState(),
    val logo: FotoUiState = FotoUiState(),
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
    val confirmandoDescarte: Boolean = false,
    val salir: Boolean = false,
    val sesionCerrada: Boolean = false,
) {
    val subiendoFoto: Boolean get() = portada.subiendo || logo.subiendo
}

data class FotoUiState(
    /** Lo que se ve: la foto elegida en el celular (mientras sube y después) o la URL ya guardada. */
    val imagen: String? = null,
    /** La foto ya subida a Storage: es la que se guarda al continuar. */
    val url: String? = null,
    /** Entre 0 y 1 mientras sube; null si no está subiendo. */
    val progreso: Float? = null,
    val error: ErrorRestaurante? = null,
) {
    val subiendo: Boolean get() = progreso != null
}

@HiltViewModel
class DatosLocalViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val obtenerRestaurante: ObtenerRestauranteUseCase,
    private val guardarDatosLocal: GuardarDatosLocalUseCase,
    private val reenviarARevision: ReenviarARevisionUseCase,
    private val subirFoto: SubirFotoUseCase,
    private val cerrarSesion: CerrarSesionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DatosLocalUiState(modo = savedStateHandle.toRoute<RutaDatosLocal>().modo)
    )
    val uiState: StateFlow<DatosLocalUiState> = _uiState.asStateFlow()

    // Lo que había guardado al abrir la pantalla, para saber si al salir hay cambios que se perderían.
    private var valoresGuardados: ValoresFormulario? = null

    init {
        cargarLocal()
    }

    fun onReintentarCarga() = cargarLocal()

    fun onCambiarNombre(nombre: String) =
        cambiarCampo(CampoLocal.NOMBRE) { it.copy(nombre = nombre.take(DatosLocal.MAX_NOMBRE)) }

    fun onElegirCategoria(categoria: CategoriaRestaurante) =
        cambiarCampo(CampoLocal.CATEGORIA) { it.copy(categoria = categoria) }

    fun onCambiarDireccion(direccion: String) = cambiarCampo(CampoLocal.DIRECCION) {
        it.copy(direccion = direccion.take(DatosLocal.MAX_DIRECCION), direccionPorCorregir = false)
    }

    fun onCambiarTelefono(telefono: String) =
        cambiarCampo(CampoLocal.TELEFONO) { it.copy(telefono = telefono.take(MAX_TELEFONO)) }

    fun onMoverPunto() = _uiState.update { it.copy(eligiendoUbicacion = true) }

    fun onCancelarUbicacion() = _uiState.update { it.copy(eligiendoUbicacion = false) }

    fun onUbicacionElegida(ubicacion: Ubicacion) =
        _uiState.update { it.copy(ubicacion = ubicacion, eligiendoUbicacion = false, direccionPorCorregir = false) }

    // La foto se sube apenas se elige. Si falla, vuelve a verse la que ya estaba subida (o ninguna).
    // No se suben fotos mientras se guarda: al guardar se borran de Storage las que no se usan.
    fun onFotoElegida(tipo: TipoFoto, imagenLocal: String) {
        val estado = _uiState.value
        if (estado.guardando || estado.foto(tipo).subiendo) return

        // Elegir una portada corrige la marca de «falta la portada», igual que escribir en un campo.
        if (tipo == TipoFoto.PORTADA) cambiarCampo(CampoLocal.PORTADA) { it }
        cambiarFoto(tipo) { it.copy(imagen = imagenLocal, progreso = 0f, error = null) }
        viewModelScope.launch {
            try {
                val url = subirFoto(tipo, imagenLocal) { fraccion ->
                    cambiarFoto(tipo) { foto -> if (foto.subiendo) foto.copy(progreso = fraccion) else foto }
                }
                cambiarFoto(tipo) { it.copy(url = url, progreso = null) }
            } catch (e: ErrorRestaurante) {
                cambiarFoto(tipo) { it.copy(imagen = it.url, progreso = null, error = e) }
            }
        }
    }

    // En el alta y al editar solo guarda; en R7 guarda y reenvía a revisión, aunque no haya cambios:
    // el arreglo pudo estar en el horario o en la carta.
    fun onContinuar() {
        val estado = _uiState.value
        if (estado.guardando || estado.subiendoFoto) return

        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            try {
                val guardar = if (estado.modo == ModoFormulario.CORREGIR) reenviarARevision::invoke else guardarDatosLocal::invoke
                guardar(
                    estado.nombre,
                    estado.categoria,
                    estado.direccion,
                    estado.ubicacion,
                    estado.telefono,
                    estado.portada.url,
                    estado.logo.url,
                )
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

    fun onVolver() {
        val estado = _uiState.value
        when {
            // En el alta la cuenta ya está creada y verificada: volver al paso 1 no tiene sentido, así que
            // «atrás» cierra la sesión. Al volver a entrar, el arranque trae de nuevo a este paso.
            estado.modo == ModoFormulario.ALTA -> {
                cerrarSesion()
                _uiState.update { it.copy(sesionCerrada = true) }
            }
            estado.hayCambios() -> _uiState.update { it.copy(confirmandoDescarte = true) }
            else -> _uiState.update { it.copy(salir = true) }
        }
    }

    fun onSeguirEditando() = _uiState.update { it.copy(confirmandoDescarte = false) }

    fun onDescartarCambios() = _uiState.update { it.copy(confirmandoDescarte = false, salir = true) }

    private fun cargarLocal() {
        _uiState.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            try {
                val restaurante = obtenerRestaurante()
                val datos = restaurante?.datos
                _uiState.update { estado ->
                    if (datos == null) {
                        estado.copy(cargando = false)
                    } else {
                        val corrigiendo = estado.modo == ModoFormulario.CORREGIR
                        estado.copy(
                            cargando = false,
                            rechazo = restaurante.rechazo.takeIf { corrigiendo },
                            direccionPorCorregir = corrigiendo &&
                                restaurante.rechazo?.motivo == MotivoRechazo.DIRECCION_NO_VERIFICABLE,
                            portada = FotoUiState(imagen = datos.portadaUrl, url = datos.portadaUrl),
                            logo = FotoUiState(imagen = datos.logoUrl, url = datos.logoUrl),
                            nombre = datos.nombre,
                            categoria = datos.categoria,
                            direccion = datos.direccion,
                            ubicacion = datos.ubicacion,
                            telefono = datos.telefono,
                        )
                    }
                }
                valoresGuardados = _uiState.value.valores()
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

    private data class ValoresFormulario(
        val nombre: String,
        val categoria: CategoriaRestaurante?,
        val direccion: String,
        val ubicacion: Ubicacion,
        val telefono: String,
        val portadaUrl: String?,
        val logoUrl: String?,
    )

    private fun DatosLocalUiState.valores() =
        ValoresFormulario(nombre, categoria, direccion, ubicacion, telefono, portada.url, logo.url)

    // Una foto a medio subir también cuenta: si se sale, se pierde.
    private fun DatosLocalUiState.hayCambios(): Boolean = subiendoFoto || valores() != valoresGuardados

    private fun DatosLocalUiState.foto(tipo: TipoFoto): FotoUiState = when (tipo) {
        TipoFoto.PORTADA -> portada
        TipoFoto.LOGO -> logo
    }

    private fun cambiarFoto(tipo: TipoFoto, cambio: (FotoUiState) -> FotoUiState) {
        _uiState.update { estado ->
            when (tipo) {
                TipoFoto.PORTADA -> estado.copy(portada = cambio(estado.portada))
                TipoFoto.LOGO -> estado.copy(logo = cambio(estado.logo))
            }
        }
    }

    private companion object {
        // «+51 987 654 321» con espacios es lo más largo que tiene sentido escribir.
        const val MAX_TELEFONO = 16
    }
}
