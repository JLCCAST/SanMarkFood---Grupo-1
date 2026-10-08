package com.equipo.sanmarkfood.restaurante.presentation.menu

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class CamaraUiState(
    val tomando: Boolean = false,
    val foto: String? = null,
    val error: Boolean = false,
)

@HiltViewModel
class CamaraViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CamaraUiState())
    val uiState: StateFlow<CamaraUiState> = _uiState.asStateFlow()

    fun onTomarFoto() = _uiState.update { it.copy(tomando = true, error = false) }

    fun onFotoLista(foto: String) = _uiState.update { it.copy(tomando = false, foto = foto, error = false) }

    fun onErrorFoto() = _uiState.update { it.copy(tomando = false, error = true) }
}
