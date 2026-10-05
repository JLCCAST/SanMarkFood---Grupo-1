package com.equipo.sanmarkfood.comensal.presentation.perfil

import android.net.Uri
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.perfil.Comensal
import com.equipo.sanmarkfood.comensal.domain.model.perfil.Direccion
import com.equipo.sanmarkfood.comensal.domain.repository.AuthRepository
import com.equipo.sanmarkfood.comensal.domain.usecase.perfil.ActualizarDatosPerfilUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.perfil.ActualizarPreferenciasNotificacionUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.perfil.AgregarDireccionUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.perfil.EliminarDireccionUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.perfil.ObservarPerfilUseCase
import com.equipo.sanmarkfood.comensal.domain.usecase.perfil.SubirFotoPerfilUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class PerfilUiState(
    val comensal: Comensal = Comensal(),
    val isLoading: Boolean = false,
    val subiendoFoto: Boolean = false,
    @StringRes val error: Int? = null,
    val guardadoExitoso: Boolean = false
) {
    // Se usa cuando falte un dato esencial para completar un pedido o reserva.
    // Pensado para que la futura pantalla de pedidos lo consulte antes de confirmar.
    val perfilIncompleto: Boolean
        get() = comensal.nombre.isBlank() || comensal.telefono.isBlank()
}

@HiltViewModel
class ComensalViewModel @Inject constructor(
    private val observarPerfil: ObservarPerfilUseCase,
    private val actualizarDatosPerfil: ActualizarDatosPerfilUseCase,
    private val subirFotoPerfil: SubirFotoPerfilUseCase,
    private val agregarDireccionUseCase: AgregarDireccionUseCase,
    private val eliminarDireccionUseCase: EliminarDireccionUseCase,
    private val actualizarPreferencias: ActualizarPreferenciasNotificacionUseCase,
    private val repositorioAuth: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    private val uid: String?
        get() = repositorioAuth.currentUser?.uid

    // Cuenta cuyos datos están en pantalla; sirve para no mostrar datos de otra cuenta.
    private var uidCargado: String? = null

    // Escucha activa del perfil en Firestore; se cancela si cambia de cuenta.
    private var perfilJob: Job? = null

    // Datos de solo lectura de la cuenta (vienen de la sesión, no de Firestore).
    val correo: String
        get() = repositorioAuth.currentUser?.correo.orEmpty()

    val correoVerificado: Boolean
        get() = repositorioAuth.currentUser?.correoVerificado == true

    /** Nombre escrito al registrarse. */
    val nombreRegistro: String
        get() = repositorioAuth.currentUser?.nombre.orEmpty()

    /** Mes y año en que se creó la cuenta, por ejemplo "septiembre 2026". */
    val miembroDesde: String
        get() {
            val millis = repositorioAuth.currentUser?.fechaCreacion ?: return ""
            return SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("es-PE")).format(Date(millis))
        }

    fun cargarPerfil() {
        val currentUid = uid ?: return
        if (uidCargado == currentUid && perfilJob?.isActive == true) return

        uidCargado = currentUid
        _uiState.value = PerfilUiState(isLoading = true)

        perfilJob?.cancel()
        perfilJob = viewModelScope.launch {
            observarPerfil(currentUid)
                .catch { e ->
                    Log.e(TAG, "cargarPerfil", e)
                    _uiState.update { it.copy(isLoading = false, error = R.string.error_perfil_cargar) }
                }
                .collect { comensal ->
                    _uiState.update { it.copy(comensal = comensal, isLoading = false, error = null) }
                }
        }
    }

    fun actualizarDatos(nombre: String, telefono: String) {
        val currentUid = uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, guardadoExitoso = false) }
            actualizarDatosPerfil(currentUid, nombre, telefono)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, guardadoExitoso = true) }
                }
                .onFailure { e ->
                    Log.e(TAG, "actualizarDatos", e)
                    _uiState.update { it.copy(isLoading = false, error = R.string.error_perfil_guardar) }
                }
        }
    }

    fun agregarDireccion(direccion: Direccion) {
        val currentUid = uid ?: return
        val actuales = _uiState.value.comensal.direcciones
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            agregarDireccionUseCase(currentUid, actuales, direccion)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                }
                .onFailure { e ->
                    Log.e(TAG, "agregarDireccion", e)
                    _uiState.update { it.copy(isLoading = false, error = R.string.error_perfil_direccion_guardar) }
                }
        }
    }

    fun eliminarDireccion(direccion: Direccion) {
        val currentUid = uid ?: return
        val actuales = _uiState.value.comensal.direcciones
        viewModelScope.launch {
            eliminarDireccionUseCase(currentUid, actuales, direccion)
                .onFailure { e ->
                    Log.e(TAG, "eliminarDireccion", e)
                    setError(R.string.error_perfil_direccion_eliminar)
                }
        }
    }

    fun actualizarPreferenciasNotificacion(notificarReservas: Boolean, notificarResenas: Boolean) {
        val currentUid = uid ?: return
        viewModelScope.launch {
            actualizarPreferencias(currentUid, notificarReservas, notificarResenas)
                .onFailure { e ->
                    Log.e(TAG, "actualizarPreferenciasNotificacion", e)
                    setError(R.string.error_perfil_preferencias)
                }
        }
    }

    fun subirFoto(uri: Uri) {
        val currentUid = uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(subiendoFoto = true, error = null) }
            subirFotoPerfil(currentUid, uri)
                .onSuccess {
                    _uiState.update { it.copy(subiendoFoto = false) }
                }
                .onFailure { e ->
                    Log.e(TAG, "subirFoto", e)
                    setError(R.string.error_perfil_foto)
                }
        }
    }

    fun clearGuardadoExitoso() = _uiState.update { it.copy(guardadoExitoso = false) }

    private fun setError(@StringRes msg: Int) =
        _uiState.update { it.copy(isLoading = false, subiendoFoto = false, error = msg) }

    override fun onCleared() {
        super.onCleared()
        perfilJob?.cancel()
    }

    private companion object {
        const val TAG = "ComensalVM"
    }
}