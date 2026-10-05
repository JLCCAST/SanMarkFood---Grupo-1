package com.equipo.sanmarkfood.restaurante.presentation.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.equipo.sanmarkfood.restaurante.core.navigation.VerificarCorreo
import com.equipo.sanmarkfood.restaurante.domain.model.auth.ErrorAuth
import com.equipo.sanmarkfood.restaurante.domain.usecase.auth.ComprobarVerificacionUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.auth.ReenviarVerificacionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VerificarCorreoUiState(
    val correo: String = "",
    val comprobando: Boolean = false,
    val reenviando: Boolean = false,
    val reenviado: Boolean = false,
    val error: ErrorAuth? = null,
    val verificado: Boolean = false,
) {
    val ocupado: Boolean get() = comprobando || reenviando
}

@HiltViewModel
class VerificarCorreoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val comprobarVerificacion: ComprobarVerificacionUseCase,
    private val reenviarVerificacion: ReenviarVerificacionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        VerificarCorreoUiState(correo = savedStateHandle.toRoute<VerificarCorreo>().correo)
    )
    val uiState: StateFlow<VerificarCorreoUiState> = _uiState.asStateFlow()

    fun onYaLoConfirme() {
        if (_uiState.value.ocupado) return

        _uiState.update { it.copy(comprobando = true, reenviado = false, error = null) }
        viewModelScope.launch {
            try {
                comprobarVerificacion()
                _uiState.update { it.copy(comprobando = false, verificado = true) }
            } catch (e: ErrorAuth) {
                _uiState.update { it.copy(comprobando = false, error = e) }
            }
        }
    }

    fun onReenviarEnlace() {
        if (_uiState.value.ocupado) return

        _uiState.update { it.copy(reenviando = true, reenviado = false, error = null) }
        viewModelScope.launch {
            try {
                reenviarVerificacion()
                _uiState.update { it.copy(reenviando = false, reenviado = true) }
            } catch (e: ErrorAuth) {
                _uiState.update { it.copy(reenviando = false, error = e) }
            }
        }
    }
}
