package com.equipo.sanmarkfood.comensal.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.comensal.data.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val needsVerification: Boolean = false,
    val verificationEmail: String = "",
    val resendCooldown: Int = 0,
    // Recuperación de contraseña (SCRUM-82)
    val resetLoading: Boolean = false,
    val resetError: String? = null,
    val resetEmailSent: Boolean = false,
    // Exploración sin cuenta (SCRUM-154)
    val isGuest: Boolean = false
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var cooldownJob: Job? = null

    private fun initialState(): AuthUiState {
        val user = repository.currentUser ?: return AuthUiState()
        return if (user.isEmailVerified) {
            AuthUiState(isLoggedIn = true)
        } else {
            AuthUiState(needsVerification = true, verificationEmail = user.email.orEmpty())
        }
    }

    fun register(name: String, email: String, password: String, acceptedTerms: Boolean) {
        val problem = when {
            name.isBlank() -> "Escribe tu nombre"
            !isValidEmail(email) -> "Correo no válido"
            password.length < 8 -> "La contraseña debe tener al menos 8 caracteres"
            password.none { it.isDigit() } -> "La contraseña debe incluir al menos un número"
            !acceptedTerms -> "Debes aceptar los términos y la política de privacidad"
            else -> null
        }
        if (problem != null) return setError(problem)

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            repository.register(name.trim(), email.trim(), password)
                .onSuccess { showVerification(email.trim(), startCooldown = true) }
                .onFailure { _uiState.value = AuthUiState(error = mapError(it)) }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isEmpty()) {
            return setError("Ingresa tu correo y tu contraseña.")
        }
        if (!isValidEmail(email)) return setError("Correo no válido")

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            repository.login(email.trim(), password)
                .onSuccess { user ->
                    if (user.isEmailVerified) {
                        _uiState.value = AuthUiState(isLoggedIn = true)
                    } else {
                        showVerification(user.email ?: email.trim(), startCooldown = false)
                    }
                }
                .onFailure { _uiState.value = AuthUiState(error = mapError(it)) }
        }
    }

    /** Botón "Ya verifiqué mi correo". */
    fun checkVerified() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.reloadAndCheckVerified()
                .onSuccess { verified ->
                    _uiState.update {
                        if (verified) {
                            AuthUiState(isLoggedIn = true)
                        } else {
                            it.copy(
                                isLoading = false,
                                error = "Todavía no verificaste tu correo. Abre el enlace que te enviamos."
                            )
                        }
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = mapError(e)) }
                }
        }
    }

    /** Botón "Reenviar correo" (bloqueado mientras corre la espera de 60 s). */
    fun resendVerification() {
        if (_uiState.value.resendCooldown > 0) return
        viewModelScope.launch {
            repository.sendVerification()
                .onSuccess {
                    _uiState.update { it.copy(error = null) }
                    startCooldown()
                }
                .onFailure { e -> setError(mapError(e)) }
        }
    }

    /** Botón "Cambiar correo": cierra la sesión pendiente y vuelve al formulario. */
    fun changeEmail() {
        cooldownJob?.cancel()
        repository.logout()
        _uiState.value = AuthUiState()
    }

    fun logout() {
        cooldownJob?.cancel()
        repository.logout()
        _uiState.value = AuthUiState()
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    /** Botón "Enviar enlace" del diálogo de recuperar contraseña. */
    fun sendPasswordReset(email: String) {
        if (email.isBlank()) return setResetError("Escribe tu correo")
        if (!isValidEmail(email)) return setResetError("Correo no válido")

        viewModelScope.launch {
            _uiState.update {
                it.copy(resetLoading = true, resetError = null, resetEmailSent = false)
            }
            repository.sendPasswordReset(email.trim())
                .onSuccess {
                    _uiState.update { it.copy(resetLoading = false, resetEmailSent = true) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(resetLoading = false, resetError = mapResetError(e))
                    }
                }
        }
    }

    /** Limpia el estado del diálogo de recuperación al abrirlo o cerrarlo. */
    fun clearReset() = _uiState.update {
        it.copy(resetLoading = false, resetError = null, resetEmailSent = false)
    }

    /** Botón "Explorar sin cuenta": entra como invitado, sin usar Firebase. */
    fun continueAsGuest() {
        _uiState.value = AuthUiState(isGuest = true)
    }

    /** Sale del modo invitado y vuelve al flujo normal de login o registro. */
    fun exitGuest() {
        _uiState.value = AuthUiState()
    }

    private fun showVerification(email: String, startCooldown: Boolean) {
        _uiState.value = AuthUiState(needsVerification = true, verificationEmail = email)
        if (startCooldown) startCooldown()
    }

    private fun startCooldown(seconds: Int = 60) {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (left in seconds downTo 1) {
                _uiState.update { it.copy(resendCooldown = left) }
                delay(1000)
            }
            _uiState.update { it.copy(resendCooldown = 0) }
        }
    }

    private fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    private fun setError(msg: String) = _uiState.update { it.copy(error = msg) }

    private fun setResetError(msg: String) = _uiState.update { it.copy(resetError = msg) }

    private fun mapError(e: Throwable): String = when (e) {
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo"
        is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil"
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos"
        is FirebaseTooManyRequestsException -> "Demasiados intentos. Espera un momento e inténtalo de nuevo"
        is FirebaseNetworkException -> "Sin conexión a internet"
        else -> "Ocurrió un error. Inténtalo de nuevo"
    }

    /** Mensajes propios de la recuperación (no valen los de "contraseña incorrecta"). */
    private fun mapResetError(e: Throwable): String = when (e) {
        is FirebaseAuthInvalidUserException -> "No encontramos una cuenta con ese correo"
        is FirebaseAuthInvalidCredentialsException -> "Correo no válido"
        else -> mapError(e)
    }
}