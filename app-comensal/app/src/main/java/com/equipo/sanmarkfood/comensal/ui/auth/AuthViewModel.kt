package com.equipo.sanmarkfood.comensal.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.comensal.data.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(isLoggedIn = repository.currentUser != null)
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun register(email: String, password: String, confirm: String) {
        validate(email, password)?.let { return setError(it) }
        if (password != confirm) return setError("Las contraseñas no coinciden")
        launchAuth { repository.register(email.trim(), password) }
    }

    fun login(email: String, password: String) {
        validate(email, password)?.let { return setError(it) }
        launchAuth { repository.login(email.trim(), password) }
    }

    fun logout() {
        repository.logout()
        _uiState.value = AuthUiState()
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    private fun validate(email: String, password: String): String? = when {
        !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Correo no válido"
        password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        else -> null
    }

    private fun setError(msg: String) = _uiState.update { it.copy(error = msg) }

    private fun launchAuth(block: suspend () -> Result<*>) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            block()
                .onSuccess { _uiState.value = AuthUiState(isLoggedIn = true) }
                .onFailure { _uiState.value = AuthUiState(error = mapError(it)) }
        }
    }

    private fun mapError(e: Throwable): String = when (e) {
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo"
        is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil"
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos"
        is FirebaseNetworkException -> "Sin conexión a internet"
        else -> "Ocurrió un error. Inténtalo de nuevo"
    }
}