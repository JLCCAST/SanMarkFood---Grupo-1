package com.equipo.sanmarkfood.comensal.ui.perfil

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.comensal.data.Comensal
import com.equipo.sanmarkfood.comensal.data.ComensalRepository
import com.equipo.sanmarkfood.comensal.data.Direccion
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PerfilUiState(
    val comensal: Comensal = Comensal(),
    val isLoading: Boolean = false,
    val subiendoFoto: Boolean = false,
    val error: String? = null,
    val guardadoExitoso: Boolean = false
) {
    // Se usa cuando falte un dato esencial para completar un pedido o reserva.
    // Pensado para que la futura pantalla de pedidos lo consulte antes de confirmar.
    val perfilIncompleto: Boolean
        get() = comensal.nombre.isBlank() || comensal.telefono.isBlank()
}

class ComensalViewModel(
    private val repository: ComensalRepository = ComensalRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    private val uid: String?
        get() = auth.currentUser?.uid

    // Cuenta cuyos datos están en pantalla; sirve para no mostrar datos de otra cuenta.
    private var uidCargado: String? = null

    // Datos de solo lectura que vienen de Firebase Auth.
    val correo: String
        get() = auth.currentUser?.email.orEmpty()

    val correoVerificado: Boolean
        get() = auth.currentUser?.isEmailVerified == true

    /** Nombre escrito al registrarse (se guardó en Firebase Auth). */
    val nombreRegistro: String
        get() = auth.currentUser?.displayName.orEmpty()

    /** Mes y año en que se creó la cuenta, por ejemplo "septiembre 2026". */
    val miembroDesde: String
        get() {
            val millis = auth.currentUser?.metadata?.creationTimestamp ?: return ""
            return SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("es-PE")).format(Date(millis))
        }

    fun cargarPerfil() {
        val currentUid = uid ?: return
        if (uidCargado != currentUid) {
            uidCargado = currentUid
            _uiState.value = PerfilUiState()
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.obtenerPerfil(currentUid)
                .onSuccess { comensal ->
                    _uiState.update { it.copy(comensal = comensal, isLoading = false) }
                }
                .onFailure { e ->
                    Log.e(TAG, "cargarPerfil", e)
                    _uiState.update { it.copy(isLoading = false, error = "No se pudo cargar tu perfil") }
                }
        }
    }

    fun actualizarDatos(nombre: String, telefono: String) {
        val currentUid = uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, guardadoExitoso = false) }
            repository.actualizarPerfil(currentUid, nombre, telefono)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            comensal = it.comensal.copy(nombre = nombre, telefono = telefono),
                            isLoading = false,
                            guardadoExitoso = true
                        )
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "actualizarDatos", e)
                    _uiState.update { it.copy(isLoading = false, error = "No se pudo guardar. Intenta de nuevo") }
                }
        }
    }

    fun agregarDireccion(direccion: Direccion) {
        val currentUid = uid ?: return
        val nuevasDirecciones = _uiState.value.comensal.direcciones + direccion
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.actualizarDirecciones(currentUid, nuevasDirecciones)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            comensal = it.comensal.copy(direcciones = nuevasDirecciones),
                            isLoading = false
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, error = "No se pudo guardar la dirección") }
                }
        }
    }

    fun eliminarDireccion(direccion: Direccion) {
        val currentUid = uid ?: return
        val nuevasDirecciones = _uiState.value.comensal.direcciones - direccion
        viewModelScope.launch {
            repository.actualizarDirecciones(currentUid, nuevasDirecciones)
                .onSuccess {
                    _uiState.update { it.copy(comensal = it.comensal.copy(direcciones = nuevasDirecciones)) }
                }
                .onFailure { setError("No se pudo eliminar la dirección") }
        }
    }

    fun actualizarPreferenciasNotificacion(notificarReservas: Boolean, notificarResenas: Boolean) {
        val currentUid = uid ?: return
        viewModelScope.launch {
            repository.actualizarPreferenciasNotificacion(currentUid, notificarReservas, notificarResenas)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            comensal = it.comensal.copy(
                                notificarReservas = notificarReservas,
                                notificarResenas = notificarResenas
                            )
                        )
                    }
                }
                .onFailure { setError("No se pudieron guardar las preferencias") }
        }
    }

    fun subirFoto(uri: Uri) {
        val currentUid = uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(subiendoFoto = true, error = null) }
            repository.subirFoto(currentUid, uri)
                .onSuccess { url ->
                    repository.actualizarFoto(currentUid, url)
                        .onSuccess {
                            _uiState.update {
                                it.copy(comensal = it.comensal.copy(fotoUrl = url), subiendoFoto = false)
                            }
                        }
                        .onFailure { e ->
                            Log.e(TAG, "actualizarFoto", e)
                            setError("No se pudo guardar la foto")
                        }
                }
                .onFailure { e ->
                    Log.e(TAG, "subirFoto", e)
                    setError("No se pudo subir la foto")
                }
        }
    }

    fun clearGuardadoExitoso() = _uiState.update { it.copy(guardadoExitoso = false) }

    private fun setError(msg: String) =
        _uiState.update { it.copy(isLoading = false, subiendoFoto = false, error = msg) }

    private companion object {
        const val TAG = "ComensalVM"
    }
}