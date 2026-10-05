package com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
// La ruta se llama igual que el modelo de dominio DatosLocal, que también se usa aquí.
import com.equipo.sanmarkfood.restaurante.core.navigation.DatosLocal as RutaDatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CampoLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EventoVerificacion
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.LARGO_CODIGO_SMS
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.MotivoRechazo
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Rechazo
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.TipoFoto
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Ubicacion
import com.equipo.sanmarkfood.restaurante.domain.usecase.auth.CerrarSesionUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.gestion_restaurante.EnviarCodigoSmsUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.gestion_restaurante.GuardarDatosLocalUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.gestion_restaurante.ObtenerRestauranteUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.gestion_restaurante.ReenviarARevisionUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.gestion_restaurante.SubirFotoUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.gestion_restaurante.VerificarCodigoSmsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val esperandoOtp: Boolean = false,
    val codigoIngresado: String = "",
    val errorOtp: ErrorRestaurante? = null,
    val enviandoCodigo: Boolean = false,
    val verificandoCodigo: Boolean = false,
    val segundosParaReenviar: Int = 0,
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
    private val enviarCodigoSms: EnviarCodigoSmsUseCase,
    private val verificarCodigoSms: VerificarCodigoSmsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DatosLocalUiState(modo = savedStateHandle.toRoute<RutaDatosLocal>().modo)
    )
    val uiState: StateFlow<DatosLocalUiState> = _uiState.asStateFlow()

    // Lo que había guardado al abrir la pantalla, para saber si al salir hay cambios que se perderían.
    private var valoresGuardados: ValoresFormulario? = null

    private var envioCodigo: Job? = null
    private var cuentaReenvio: Job? = null

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
        viewModelScope.launch { guardarFormulario(telefonoRecienVerificado = false) }
    }

    fun onCambiarCodigo(codigo: String) = _uiState.update {
        it.copy(codigoIngresado = codigo.filter { c -> c in '0'..'9' }.take(LARGO_CODIGO_SMS), errorOtp = null)
    }

    fun onVerificarCodigo() {
        val estado = _uiState.value
        if (estado.verificandoCodigo || estado.enviandoCodigo) return

        _uiState.update { it.copy(verificandoCodigo = true, errorOtp = null) }
        viewModelScope.launch {
            try {
                verificarCodigoSms(estado.codigoIngresado)
            } catch (e: ErrorRestaurante) {
                _uiState.update { it.copy(verificandoCodigo = false, errorOtp = e) }
                return@launch
            }
            guardarFormulario(telefonoRecienVerificado = true)
        }
    }

    fun onReenviarCodigo() {
        val estado = _uiState.value
        if (estado.enviandoCodigo || estado.verificandoCodigo || estado.segundosParaReenviar > 0) return
        enviarCodigo(reenviar = true)
    }

    fun onCancelarVerificacion() {
        if (_uiState.value.verificandoCodigo) return
        terminarVerificacion()
    }

    private suspend fun guardarFormulario(telefonoRecienVerificado: Boolean) {
        val estado = _uiState.value
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
            terminarVerificacion()
            _uiState.update { it.copy(guardando = false, guardado = true) }
        } catch (e: ErrorRestaurante.TelefonoSinVerificar) {
            if (telefonoRecienVerificado) {
                terminarVerificacion()
                _uiState.update { it.copy(guardando = false, error = ErrorRestaurante.Desconocido) }
            } else {
                enviarCodigo(reenviar = false)
            }
        } catch (e: ErrorRestaurante.DatosInvalidos) {
            terminarVerificacion()
            _uiState.update { it.copy(guardando = false, camposInvalidos = e.campos, error = e) }
        } catch (e: ErrorRestaurante) {
            terminarVerificacion()
            _uiState.update { it.copy(guardando = false, error = e) }
        }
    }

    private fun enviarCodigo(reenviar: Boolean) {
        envioCodigo?.cancel()
        _uiState.update { it.copy(enviandoCodigo = true, errorOtp = null) }
        envioCodigo = viewModelScope.launch {
            try {
                enviarCodigoSms(_uiState.value.telefono, reenviar).collect { alRecibirEvento(it) }
            } catch (e: ErrorRestaurante.DatosInvalidos) {
                terminarVerificacion()
                _uiState.update { it.copy(guardando = false, camposInvalidos = e.campos, error = e) }
            } catch (e: ErrorRestaurante) {
                if (_uiState.value.esperandoOtp) {
                    _uiState.update { it.copy(enviandoCodigo = false, errorOtp = e) }
                } else {
                    terminarVerificacion()
                    _uiState.update { it.copy(guardando = false, error = e) }
                }
            }
        }
    }

    private fun alRecibirEvento(evento: EventoVerificacion) {
        when (evento) {
            EventoVerificacion.CodigoEnviado -> {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        esperandoOtp = true,
                        enviandoCodigo = false,
                        codigoIngresado = "",
                        errorOtp = null,
                    )
                }
                iniciarCuentaReenvio()
            }

            is EventoVerificacion.CodigoRecibido -> if (!_uiState.value.verificandoCodigo) {
                onCambiarCodigo(evento.codigo)
                onVerificarCodigo()
            }

            EventoVerificacion.Verificado -> if (!_uiState.value.verificandoCodigo) {
                _uiState.update { it.copy(verificandoCodigo = true) }
                viewModelScope.launch { guardarFormulario(telefonoRecienVerificado = true) }
            }
        }
    }

    private fun iniciarCuentaReenvio() {
        cuentaReenvio?.cancel()
        cuentaReenvio = viewModelScope.launch {
            for (segundos in SEGUNDOS_PARA_REENVIAR downTo 1) {
                _uiState.update { it.copy(segundosParaReenviar = segundos) }
                delay(1_000)
            }
            _uiState.update { it.copy(segundosParaReenviar = 0) }
        }
    }

    private fun terminarVerificacion() {
        envioCodigo?.cancel()
        cuentaReenvio?.cancel()
        _uiState.update {
            it.copy(
                esperandoOtp = false,
                codigoIngresado = "",
                errorOtp = null,
                enviandoCodigo = false,
                verificandoCodigo = false,
                segundosParaReenviar = 0,
            )
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
        const val SEGUNDOS_PARA_REENVIAR = 60
    }
}
